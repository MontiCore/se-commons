/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.queue;

import com.google.gson.Gson;
import de.monticore.gradle.internal.io.PrefixStream;
import de.monticore.gradle.internal.io.PrintStreamThreadProxy;
import de.monticore.gradle.internal.isolation.IsolatedURLClassLoader;
import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.api.services.BuildServiceRegistry;
import org.gradle.execution.RunRootBuildWorkBuildOperationType;
import org.gradle.internal.Cast;
import org.gradle.internal.build.event.BuildEventListenerRegistryInternal;
import org.gradle.internal.instantiation.InstantiatorFactory;
import org.gradle.internal.isolated.IsolationScheme;
import org.gradle.internal.isolation.Isolatable;
import org.gradle.internal.isolation.IsolatableFactory;
import org.gradle.internal.operations.*;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.serialize.Decoder;
import org.gradle.internal.serialize.InputStreamBackedDecoder;
import org.gradle.internal.serialize.OutputStreamBackedEncoder;
import org.gradle.internal.serialize.Serializer;
import org.gradle.internal.service.ServiceLookup;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import org.gradle.workers.internal.ActionExecutionSpecFactory;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Service managing the cached isolators and their backing queue
 */
public abstract class CachedQueueService
        implements ICachedQueueService, BuildService<BuildServiceParameters.None>, AutoCloseable, BuildOperationListener {

  static CachedQueueService INSTANCE;

  protected Logger logger = Logging.getLogger(CachedQueueService.class);

  protected CachedIsolationStats stats = new CachedIsolationStats();


  public void init(Gradle gradle) {
    this.init(((GradleInternal) gradle).getServices());
  }

  protected void init(ServiceRegistry serviceRegistry) {
    logger.debug("Initializing CachedQueueService");
    if (INSTANCE == null) {
      INSTANCE = this;
    } else if (INSTANCE != this) {
      logger.warn("Possibly overriding the CachedQueueService instance");
      INSTANCE = this;
    }
    this.serviceRegistry = Objects.requireNonNull(serviceRegistry);
    this.providerSelf = (Provider<CachedQueueService>) serviceRegistry.get(BuildServiceRegistry.class).getRegistrations().getByName(NAME).getService();
    Objects.requireNonNull(serviceRegistry.get(ActionExecutionSpecFactory.class), "ActionExecutionSpecFactory");
    Objects.requireNonNull(serviceRegistry.get(IsolatableFactory.class), "isolatableFactory");

    serviceRegistry.get(BuildEventListenerRegistryInternal.class)
            .onOperationCompletion(this.providerSelf);

  }

  /**
   * Service name
   */
  public static final String NAME = "se_cached_queue_service";

  /**
   * Time (in ms) after the last use of an isolated classloader before its
   * allocated resources are freed
   */
  protected long closeThreshold = 20 * 1000; // 20 seconds

  /**
   * We periodically clean up the open classloaders
   * Closing classloaders frees up the resources from memory
   */
  protected Timer cleanupTimer;

  protected final List<IIsolationData> internalRunners =
          Collections.synchronizedList(new LinkedList<>());

  /**
   * Unfortunately, Gradle does not allow us to limit the maximum work-actions of a WorkQueue being
   * performed
   * (without limit the max-worker for the entire project)
   */
  protected final Semaphore semaphore;

  protected int maximumLoadersFromConfig = 0;

  // Empty array of ProtectionDomain - see the doPrivileged() part below
  private static final ProtectionDomain[] NO_DOMAINS = new ProtectionDomain[0];

  // AccessControlContext without domains - see the doPrivileged() part below
  private static final AccessControlContext NO_DOMAINS_ACCESS_CONTROL_CONTEXT =
          new AccessControlContext(NO_DOMAINS);

  /**
   * WorkParameters do not support the information stored in the {@link ActualTaskInfo},
   * which is why we use this weird workaround of a UUID-key
   */
  protected Map<UUID, ActualTaskInfo<?>> taskInfoMap = new ConcurrentHashMap<>();

  protected final IsolationScheme<WorkAction<?>, WorkParameters> isolationScheme =
          new IsolationScheme<>(Cast.uncheckedCast(WorkAction.class), WorkParameters.class,
                  WorkParameters.None.class);

  public CachedQueueService() {
    this.maximumLoadersFromConfig = guessInitialMaxParallel();
    logger.debug("Starting with a maximum count of {}", maximumLoadersFromConfig);
    this.semaphore = new Semaphore(maximumLoadersFromConfig);
  }

  public synchronized void setMaxConcurrentMC(int maxParallelMC) {
    if (maximumLoadersFromConfig < maxParallelMC) {
      // The limit has been increased -> release/add some permits
      semaphore.release(maxParallelMC - maximumLoadersFromConfig);
      maximumLoadersFromConfig = maxParallelMC;
    } else if (maximumLoadersFromConfig > maxParallelMC) {
      // The max amount has been lowered -> acquire/remove some permits
      semaphore.acquireUninterruptibly(maximumLoadersFromConfig - maxParallelMC);
      maximumLoadersFromConfig = maxParallelMC;
    }
  }


  protected ServiceRegistry serviceRegistry;
  protected Provider<CachedQueueService> providerSelf;

  @Override
  public void close() throws Exception {
    this.markForErasure();
    this.cleanupOld(0);
  }


  /**
   * Get a new (auto-closing) isolated class loader.
   * In case no available class loaders are present,
   * a new instance may be created.
   */
  protected synchronized IIsolationData getLoader(Predicate<FileCollection> predicate,
                                                  Supplier<FileCollection> supplier,
                                                  String uniqueId) {
    Optional<IIsolationData> d = this.internalRunners.stream().filter(x -> !x.isRunning())
            .filter(x -> predicate.test(x.getExtraData())).findAny();
    if (d.isPresent()) {
      logger.debug("Reusing existing runner " + d.get().getUUID());
      d.get().setRunning(true);
      stats.track(CachedIsolationStats.EventKind.REUSE, d.get().getUUID(), maximumLoadersFromConfig, this.internalRunners, uniqueId);
      return d.get();
    }

    // TODO/Future work: Check, if we have enough memory to warrant starting a new loader or should better wait a bit

    this.cleanupOld(closeThreshold);
    IsolationData data = new IsolationData();
    data.classLoader =
            getClassLoader((URLClassLoader) Thread.currentThread().getContextClassLoader(), supplier);
    data.running = true;
    data.extraData = supplier.get();
    stats.track(CachedIsolationStats.EventKind.CREATE, data.getUUID(), maximumLoadersFromConfig, this.internalRunners, uniqueId);
    this.internalRunners.add(data);
    logger.debug("Creating new loader " + data.getUUID());
    setupTimer();
    return data;
  }

  protected synchronized void setupTimer() {
    if (cleanupTimer != null) {
      return;
    }
    cleanupTimer = new Timer();
    cleanupTimer.schedule(new TimerTask() {

      @Override
      public void run() {
        CachedQueueService.this.cleanupOld(closeThreshold);
      }
    }, 2 * 1000, 2 * 1000);
  }

  public void setCloseThreshold(long closeThreshold) {
    logger.debug("Setting close threshold to {} ", closeThreshold );
    this.closeThreshold = closeThreshold;
  }

  /**
   * Close unused classloaders to free up memory
   */
  protected synchronized void cleanupOld(long pCloseThreshold) {
    // TODO/Future work: We could use the buildListener and currently pending to guess-estimate,
    // whether we still need to run a task with this classloader

    long threshold = System.currentTimeMillis() - pCloseThreshold;
    Iterator<IIsolationData> isolated = this.internalRunners.iterator();
    logger.debug("Running cleanup thread");
    while (isolated.hasNext()) {
      IIsolationData data = isolated.next();
      logger.debug(" - {} - {} - {}", data.isRunning() ? "R" : "I", data.getLastRun(), data.getUUID());
      if (!data.isRunning() && data.getLastRun() < threshold) {
        stats.track(CachedIsolationStats.EventKind.CLEANUP, data.getUUID(), maximumLoadersFromConfig, this.internalRunners);
        logger.debug("   - close ");
        cleanupGradleInternals(data.getClassLoader());
        if (data.getClassLoader() instanceof Closeable) {
          // Close closeable classloaders
          try {
            ((Closeable) data.getClassLoader()).close();
          } catch (IOException ignored) {
          }
        }
        data.cleanUp();
        isolated.remove();
      }
    }
    if (cleanupTimer != null && this.internalRunners.isEmpty()) {
      cleanupTimer.cancel();
      cleanupTimer = null;
    }
  }
  
  /**
   * Gradle stores each generated class in a cache.
   * We thus have to remove it from instantiationScheme.deserializationConstructorCache
   * and instantiationScheme.constructorSelector.constructorCache
   * @param loader the classloader to clean up after
   */
  protected void cleanupGradleInternals(ClassLoader loader) {
    try {
      // This functionality is hidden within Gradle's internal API and subject to change.
      // the following cleanup has been tested with gradle 8.14
      
      // Unfortunately, we have to use reflections as Gradle does not provide an API for
      // either clearing this cache or using a cache-less instantiator
      Field instantiationSchemeF = providerSelf.getClass().getDeclaredField("instantiationScheme");
      instantiationSchemeF.setAccessible(true);
      Object instantiationScheme = instantiationSchemeF.get(providerSelf);
      
      Field deserializationConstructorCacheF =
          instantiationScheme.getClass().getDeclaredField("deserializationConstructorCache");
      deserializationConstructorCacheF.setAccessible(true);
      clearBuildInMemoryCache(deserializationConstructorCacheF.get(instantiationScheme), loader);
      
      Field constructorSelectorF =
          instantiationScheme.getClass().getDeclaredField("constructorSelector");
      constructorSelectorF.setAccessible(true);
      Object constructorSelector = constructorSelectorF.get(instantiationScheme);
      
      Field constructorCacheF = constructorSelector.getClass().getDeclaredField("constructorCache");
      constructorCacheF.setAccessible(true);
      clearBuildInMemoryCache(constructorCacheF.get(constructorSelector), loader);
    }
    catch (Exception e) {
      logger.warn("Failed to cleanup after gradle internals. "
          + "You might notice an increased memory usage", e);
    }
  }
  
  /**
   * remove all classes loaded by a given classloader from the valuesForThisSession map/cache
   * @param deserializationConstructorCache most likely a DefaultCrossBuildInMemoryCache
   * @param loader the classloader
   * @throws ReflectiveOperationException when the internal api changes
   */
  protected void clearBuildInMemoryCache(Object deserializationConstructorCache, ClassLoader loader) throws ReflectiveOperationException {
    Field valuesForThisSessionF = deserializationConstructorCache.getClass().getSuperclass()
        .getDeclaredField("valuesForThisSession");
    valuesForThisSessionF.setAccessible(true);
    Map<Object, Object> valuesForThisSession =
        (Map<Object, Object>) valuesForThisSessionF.get(deserializationConstructorCache);
    Iterator<?> it = valuesForThisSession.keySet().iterator();
    while (it.hasNext()) {
      Object e = it.next();
      if (e instanceof Class && ((Class<?>) e).getClassLoader() == loader) {
        it.remove();
      }
    }
  }

  protected ClassLoader getClassLoader(URLClassLoader contextClassLoader,
                                       Supplier<FileCollection> supplier) {
    // Merge the extra classpath with the current classpath
    URL[] urls = Stream.concat(supplier.get().getFiles().stream().map(f -> {
      try {
        return f.toURI().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }), Arrays.stream(contextClassLoader.getURLs())).toArray(URL[]::new);
    return new IsolatedURLClassLoader(urls, contextClassLoader, getPassThroughPackages());
  }

  /**
   * @return a set of packages which may be loaded from the context classloader
   * (and thus are not isolated)
   */
  protected Set<String> getPassThroughPackages() {
    return Set.of("org.gradle");
  }

  public void doExecuteWorkAction(UUID actionUUID) {
    long timeWaited = System.currentTimeMillis();
    try {
      // In case we run into the limit of maximum concurrent MontiCore Generation actions,
      // we wait until another generation has concluded
      semaphore.acquire();
    } catch (InterruptedException e) {
      // Unable to acquire slot to run -> abort
      passThrowableAlong(e);
    }
    timeWaited = System.currentTimeMillis() - timeWaited;
    try {
      ActualTaskInfo<?> info = Objects.requireNonNull(taskInfoMap.remove(actionUUID), "WorkAction with UUID " + actionUUID + " was never registered. This is an internal error.");
      doExecuteWorkAction(info, timeWaited);
    } finally {
      semaphore.release();
    }
  }

  void doExecuteWorkAction(ActualTaskInfo<?> info, long timeWaitedForSemaphore) {

    Class<? extends WorkParameters> parameterTypeNotIsolated =
            isolationScheme.parameterTypeFor(info.workActionClass);

    // Unsafe, as they are still in the wrong classloader
    WorkParameters parametersUnsafe = parameterTypeNotIsolated == null ? null
            : info.instantiatorFactory.decorateLenient(
                    info.services)
            .newInstance(parameterTypeNotIsolated);
    if (parametersUnsafe != null) {
      info.parameterAction.execute(parametersUnsafe);
    }


    Isolatable<WorkParameters> paramIsol = serviceRegistry.get(IsolatableFactory.class).isolate(parametersUnsafe);

    IsolatableSerializerRegistryWrapper isolatableSerializerRegistry = getIsolatableSerializerRegistryWrapper();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Serializer serializer = isolatableSerializerRegistry.build(paramIsol.getClass());
    try {
      serializer.write(new OutputStreamBackedEncoder(bos), paramIsol);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize " + info.workActionClass.getName() + " " + parameterTypeNotIsolated.getName() + " with " + parametersUnsafe.getClass(), e);
    }

    String prefix = parametersUnsafe instanceof CachedIsolatedWorkQueue.WorkQueueParameters ? ((CachedIsolatedWorkQueue.WorkQueueParameters) parametersUnsafe)
            .getPrefix().getOrElse("[WA]") : "[WA]";

    String uniqueId = parametersUnsafe instanceof CachedIsolatedWorkQueue.WorkQueueParameters ? ((CachedIsolatedWorkQueue.WorkQueueParameters) parametersUnsafe).getStatsUniqueName().getOrElse("?") : "?";
    
    uniqueId += "," + timeWaitedForSemaphore + "ms";
    
    executeInClassloader(() -> {
          
          final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
          try {
            Isolatable<?> params = isolatableSerializerRegistry.readIsolatable(
                new InputStreamBackedDecoder(new ByteArrayInputStream(bos.toByteArray())));
            
            // finished params init
            
            // Create the WorkAction itself
            
            // instantiate within the new classloader
            
            // prepare instantiator to set parameters
            
            Class<? extends WorkParameters> paramTypeIsolated =
                (Class<? extends WorkParameters>) contextClassLoader.loadClass(
                    parameterTypeNotIsolated.getName());
            
            ServiceLookup instantiationServices =
                isolationScheme.servicesForImplementation(params.coerce(paramTypeIsolated),
                    info.services, Collections.emptySet(), aClass -> false);
            
            Instantiator instantiator = info.instantiatorFactory.inject(instantiationServices);
            
            // Load a fresh instance of this class
            Class<? extends WorkAction> c = (Class<? extends WorkAction>) contextClassLoader.loadClass(
                info.workActionClass.getName());
            WorkAction<?> action = instantiator.newInstance(c);
            action.execute();
          }
          catch (ClassNotFoundException | NoClassDefFoundError e) {
            // This exception might indicate a possible problem with our classloader -> ALu
            throw new RuntimeException(
                "Potential classloader issue in CL " + contextClassLoader + " with classpath "
                    + info.classPath.getFiles(), e);
          }
          catch (Exception e) {
            passThrowableAlong(e);
          }
        }, prefix, uniqueId,
        f -> f.minus(info.classPath).getFiles().isEmpty() && info.classPath.minus(f).getFiles()
            .isEmpty(), // check if the difference between the classpaths is empty
        () -> info.classPath);
  }

  /**
   * Loads a class and runs a given method in an isolated class loader
   * Requires the class and method name as Strings,
   * such that we do not load them from an existing parent classloader
   *
   * @param prefix    Prefixes this string via a {@link PrefixStream}
   * @param uniqueId  Uniqueid for tracking via the stats
   * @param predicate allows to check for additional elements, such as classpath
   * @param supplier  allows to set the additional arguments for a new loader
   */
  protected void executeInClassloader(Runnable runnable, @Nullable String prefix,
                                      @Nullable String uniqueId,
                                      Predicate<FileCollection> predicate, Supplier<FileCollection> supplier) {
    final Thread currentThread = Thread.currentThread();
    ClassLoader originalClassLoader = currentThread.getContextClassLoader();
    UUID uuid = null;
    try (IIsolationData isolationData = getLoader(predicate, supplier, uniqueId)) {
      uuid = isolationData.getUUID();
      currentThread.setContextClassLoader(isolationData.getClassLoader());

      // set the prefix for the printing
      if (prefix != null) {
        redirectStream(getOrReplaceErr(), prefix);
        redirectStream(getOrReplaceOut(), prefix);
      }
      stats.track(CachedIsolationStats.EventKind.START, uuid, maximumLoadersFromConfig, this.internalRunners, uniqueId);

      // Use doPrivileged() to avoid spawned threads inheriting the
      // AccessControlContext of the current thread.
      // No privileged actions are actually performed.
      // Reason: among the ProtectionDomains of the thread will otherwise be a
      // reference to the Isolated and/or children Groovy Classloader,
      // preventing the GC from reclaiming said Classloader with all its
      // loaded objects
      AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
        try {
          // Prepare for the cleanup after <a href="https://bugs.openjdk.org/browse/JDK-8078641">JDK-8078641</a>
          // Load the MethodHandleImpl class into the isolated classloader
          // as the GroovyInterpreter will otherwise load it within its
          // ClassLoaders, resulting in it being not accessible to this outer CL
          isolationData.getClassLoader().loadClass("java.lang.invoke.MethodHandleImpl");
        } catch (ReflectiveOperationException ignored) {
        }
        runnable.run();
        return null;
        // Continue with the modified AccessControlContext
      }, new AccessControlContext(NO_DOMAINS_ACCESS_CONTROL_CONTEXT, combiner));
    } finally {
      if (prefix != null) {
        // Reset this threads prefix printers
        getOrReplaceErr().reset();
        getOrReplaceOut().reset();
      }
      currentThread.setContextClassLoader(originalClassLoader);
      stats.track(CachedIsolationStats.EventKind.DONE, uuid, maximumLoadersFromConfig, this.internalRunners);
    }
  }

  /**
   * A {@link DomainCombiner} which removes inner class loaders from a Threads
   * {@link AccessControlContext}
   * <p>
   * The {@link #isClassLoaderOrChild(ClassLoader)} method filters said
   * child class loaders.
   */
  protected DomainCombiner combiner = (currentDomains, assignedDomains) -> {
    // The assigned Domains should be equivalent to NO_DOMAINS or null,
    // but groovy uses AccessController.doPrivileged itself, causing
    // the UpdateCheckerRunnable to be assigned its current domains
    // We thus skip them, as otherwise the context loader leaks
    final List<ProtectionDomain> combinedWithoutIsolated = new ArrayList<>();
    for (ProtectionDomain protectionDomain : currentDomains) {
      if (protectionDomain.getClassLoader() == null || !isClassLoaderOrChild(
              protectionDomain.getClassLoader())) {
        combinedWithoutIsolated.add(protectionDomain);
      }
    }
    return combinedWithoutIsolated.toArray(new ProtectionDomain[0]);
  };

  /**
   * @return true if said class loader should not be included in an {@link AccessControlContext}
   */
  protected boolean isClassLoaderOrChild(ClassLoader classloader) {
    if (classloader == null) {
      return false;
    }
    // Note: We are unable to compare using the class object due to classloaders
    return classloader.getClass().getName().equals(IsolatedURLClassLoader.class.getName())
            || classloader.getClass().getName().equals("groovy.lang.GroovyClassLoader$InnerLoader");
  }

  @SuppressWarnings("unchecked")
  protected <E extends Throwable> void passThrowableAlong(Throwable e) throws E {
    throw (E) e;
  }

  /**
   * See {@link PrintStreamThreadProxy}
   *
   * @return the wrapped error stream
   */
  protected synchronized PrintStreamThreadProxy getOrReplaceErr() {
    synchronized (System.err) { // make sure we lock onto System.err too
      if (System.err instanceof PrintStreamThreadProxy) {
        return (PrintStreamThreadProxy) System.err;
      }
      System.setErr(new PrintStreamThreadProxy(System.err));
      return (PrintStreamThreadProxy) System.err;
    }
  }

  /**
   * See {@link PrintStreamThreadProxy}
   *
   * @return the wrapped error stream
   */
  protected synchronized PrintStreamThreadProxy getOrReplaceOut() {
    synchronized (System.out) { // make sure we lock onto System.out too
      if (System.out instanceof PrintStreamThreadProxy) {
        return (PrintStreamThreadProxy) System.out;
      }
      System.setOut(new PrintStreamThreadProxy(System.out));
      return (PrintStreamThreadProxy) System.out;
    }
  }

  /**
   * Redirect a print stream into a prefix stream.
   * ThreadLocalPrintStream is used to support multiple threads
   */
  protected void redirectStream(PrintStreamThreadProxy ps, String prefix) {
    ps.setRedirect(new PrefixStream(ps.getOriginal(), prefix));
  }

  public <T extends WorkParameters> void register(UUID uuid,
                                                  Class<? extends WorkAction<T>> workActionClass, Action<? super T> parameterAction,
                                                  InstantiatorFactory instantiatorFactory, ServiceRegistry serviceRegistry,
                                                  FileCollection classPath) {

    this.taskInfoMap.put(uuid,
            new CachedQueueService.ActualTaskInfo(workActionClass, parameterAction,
                    Objects.requireNonNull(instantiatorFactory, "instantiatorFactory"),
                    Objects.requireNonNull(serviceRegistry, "serviceRegistry"), classPath));
  }

  public static class ActualTaskInfo<T extends WorkParameters> {

    final Class<? extends WorkAction<T>> workActionClass;
    final Action<? super WorkParameters> parameterAction;
    public InstantiatorFactory instantiatorFactory;
    public ServiceRegistry services;
    public FileCollection classPath;

    public ActualTaskInfo(Class<? extends WorkAction<T>> workActionClass,
                          Action<? super WorkParameters> parameterAction, InstantiatorFactory instantiatorFactory,
                          ServiceRegistry serviceRegistry, FileCollection classPath) {
      this.workActionClass = workActionClass;
      this.parameterAction = parameterAction;
      this.instantiatorFactory = instantiatorFactory;
      this.services = serviceRegistry;
      this.classPath = classPath;
    }
  }

  protected static class IsolationData implements IIsolationData {

    protected ClassLoader classLoader;

    protected boolean running;

    protected long lastRun = System.currentTimeMillis();

    protected FileCollection extraData;

    protected final UUID uuid = UUID.randomUUID();

    @Override
    public ClassLoader getClassLoader() {
      return classLoader;
    }

    @Override
    public boolean isRunning() {
      return this.running;
    }

    @Override
    public void setRunning(boolean r) {
      this.running = r;
    }

    @Override
    public FileCollection getExtraData() {
      return extraData;
    }

    @Override
    public long getLastRun() {
      return lastRun;
    }

    @Override
    public UUID getUUID() {
      return uuid;
    }

    @Override
    public void cleanUp() {
      this.classLoader = null;
      this.extraData = null;
    }

    @Override
    public void close() {
      this.running = false;
      this.lastRun = System.currentTimeMillis();
    }
  }

  protected interface IIsolationData extends AutoCloseable {

    ClassLoader getClassLoader();

    boolean isRunning();

    void setRunning(boolean r);

    FileCollection getExtraData();

    void cleanUp();

    long getLastRun();

    UUID getUUID();

    @Override
    void close();
  }

  protected static int guessInitialMaxParallel() {
    // We generously estimate 150MB of memory usage per concurrent worker execution
    // This memory footprint includes the runtime object, as well as overhead for loading classes, the jars
    // within the classpath, etc.
    long leftOverMemory = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
    // But as a note: metaspace is GCed/managed by the JVM, so we actually have no idea how many classes we could load
    final int estimated_memory_usage_in_mb = 150; // from testing, 512 MB allows ~2 parallel workers (XML DSL)
    // We always allow 2 parallel workers by default (use CONCURRENT_MC_PROPERTY to increase/decrease this value)
    // In the future: Move this limit to a per-workqueue basis - as in "do I have 150MB available?"
    return (int) Math.max(2, leftOverMemory / (estimated_memory_usage_in_mb*1000*1000));
  }

  @Override
  public void started(BuildOperationDescriptor buildOperationDescriptor, OperationStartEvent operationStartEvent) {
    // noop
  }

  @Override
  public void progress(OperationIdentifier operationIdentifier, OperationProgressEvent operationProgressEvent) {
    // noop
  }

  @Override
  public void finished(BuildOperationDescriptor buildOperationDescriptor, OperationFinishEvent operationFinishEvent) {
    if (buildOperationDescriptor.getDetails() instanceof RunRootBuildWorkBuildOperationType.Details) {
      stats.track(CachedIsolationStats.EventKind.BUILT_DONE, null, maximumLoadersFromConfig, this.internalRunners);
      this.markForErasure();
      // Note: we could/should push the stats to somewhere?
    }
  }

  public String getStats() {
    return stats.asJson(new Gson());
  }

  /**
   * Mark all internal runers as ready to be unloaded
   */
  public void markForErasure() {
    logger.debug("Marking forErasure");
    this.internalRunners.forEach(r -> ((IsolationData) r).lastRun = 0);
  }


  /**
   * Construct a new WorkQueue
   *
   * @param workerExecutor        the worker executor to use
   * @param extraClasspathElement the classpath elements to use
   * @return a new {@link WorkQueue}
   */
  public WorkQueue newWorkQueue(WorkerExecutor workerExecutor, FileCollection extraClasspathElement) {
    Objects.requireNonNull(workerExecutor, "worker executor must not be null");
    Objects.requireNonNull(serviceRegistry, "serviceRegistry must not be null");
    return new CachedIsolatedWorkQueue(workerExecutor.noIsolation(),
            serviceRegistry.get(InstantiatorFactory.class),
            Objects.requireNonNull(serviceRegistry, "serviceRegistry"),
            this.providerSelf,
            extraClasspathElement);
  }
  
  // Gradle Version compat
  protected IsolatableSerializerRegistryWrapper getIsolatableSerializerRegistryWrapper() {
    return new IsolatableSerializerRegistryWrapper(this.serviceRegistry.get(getIsolatableSerializerRegistryClass()));
  }
  
  protected Class<?> getIsolatableSerializerRegistryClass() {
    try {
      return Class.forName("org.gradle.workers.internal.IsolatableSerializerRegistry");
    }
    catch (ClassNotFoundException e) {
      try {
        return Class.forName("org.gradle.internal.snapshot.impl.IsolatableSerializerRegistry");
      }
      catch (ClassNotFoundException ex) {
        throw new IllegalStateException(ex);
      }
    }
  }
  
  /**
   * Wrapper around gradle internals.
   * Must not refer to gradle internal classes to avoid errors during plugin-ASM-phase
   */
  protected static class IsolatableSerializerRegistryWrapper {
    
    final Object instance;
    
    IsolatableSerializerRegistryWrapper(Object instance) {
      this.instance = instance;
    }
    
    <T> Serializer<T> build(Class<T> baseType) {
      try {
        return (Serializer<T>) instance.getClass().getMethod("build", Class.class)
            .invoke(instance, baseType);
      }
      catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Failed to wrap IsolatableSerializerRegistry", e);
      }
    }
    
    Isolatable<?> readIsolatable(Decoder decoder) {
      try {
        return (Isolatable<?>) instance.getClass().getMethod("readIsolatable", Decoder.class)
            .invoke(instance, decoder);
      }
      catch (ReflectiveOperationException e) {
        throw new IllegalStateException("Failed to wrap IsolatableSerializerRegistry", e);
      }
    }
  }
}
