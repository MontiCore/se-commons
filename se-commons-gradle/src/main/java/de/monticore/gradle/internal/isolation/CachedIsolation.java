/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.isolation;

import de.monticore.gradle.internal.io.PrefixStream;
import de.monticore.gradle.internal.io.PrintStreamThreadProxy;
import org.gradle.api.file.FileCollection;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A class which allows safe, concurrent execution of work units
 *  using reuseabe, isolated classloaders.
 * @param <T> an optional object associated with an isolated classloader,
 *            used to determine reusability
 */
public class CachedIsolation<T> {

  protected final List<IIsolationData<T>> internalRunners = Collections.synchronizedList(new LinkedList<>());

  /**
   * Time (in ms) after the last use of an isolated classloader before its
   * allocated resources are freed
   */
  protected final long closeThreshold = 6 * 1000; // 6 seconds

  /**
   * We periodically clean up the open classloaders
   * Closing classloaders frees up the resources from memory
   */
  protected Timer cleanupTimer;

  // staggering the startup reduces strain on the (blocking) ZipFile$Source.readFullyAt
  // When the same class is loaded multiple times in parallel,
  // the switching between contexts causes a noticeable performance impact on some machines
  protected int staggeredStartUpFixedDelay = -1;
  protected int staggeredStartUpDelayPerRunner = -1;

  /**
   * {@link System#currentTimeMillis()} of when the last startup occurred for staggered startup delay
   */
  protected long lastStartup = -1;

  /**
   * How many class loaders are waiting for their staggered startup
   */
  protected int staggerCount;

  /**
   *
   * @param staggeredStartUpFixedDelay the (fixed) time between the first and all further runners
   * @param staggeredStartUpDelayPerRunner the time between the startup of individual runners
   */
  public void setStaggeredStartupParams(int staggeredStartUpFixedDelay, int staggeredStartUpDelayPerRunner) {
    this.staggeredStartUpFixedDelay = staggeredStartUpFixedDelay;
    this.staggeredStartUpDelayPerRunner = staggeredStartUpDelayPerRunner;
  }

  protected synchronized void setupTimer() {
    if (cleanupTimer != null) return;
    cleanupTimer = new Timer();
    cleanupTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        CachedIsolation.this.cleanupOld();
      }
    }, closeThreshold, closeThreshold);
  }

  /**
   * Get a new (auto-closing) isolated class loader.
   * In case no available class loaders are present,
   * a new instance may be created.
   */
  protected synchronized IIsolationData<T> getLoader(Predicate<T> predicate, Supplier<T> supplier) {
    Optional<IIsolationData<T>> d = this.internalRunners.stream()
            .filter(x -> !x.isRunning())
            .filter(x -> predicate.test(x.getExtraData()))
            .findAny();
    if (d.isPresent()) {
      d.get().setRunning(true);
      return d.get();
    }
    this.cleanupOld();
    final long staggerWait = getWaitForStaggeredStartup();
    this.lastStartup = System.currentTimeMillis();
    if (staggerWait <= 0) {
      IsolationData<T> data = new IsolationData<>();
      data.classLoader = getClassLoader((URLClassLoader) Thread.currentThread().getContextClassLoader(), supplier);
      data.running = true;
      data.extraData = supplier.get();
      this.internalRunners.add(data);
      setupTimer();
      return data;
    } else {
      // Return a surrogate-like IsolationData
      this.staggerCount++;
      return new StaggeredIsolationData<>(() -> {
        // which when asked (out of the synchronized context) first sleeps
        try {
          Thread.sleep(staggerWait);
        } catch (InterruptedException ignored) {
        }
        this.staggerCount--;
        // and then calls the getLoader method again
        return this.getLoader(predicate, supplier);
      });
    }
  }

  /**
   * When the startup is staggered,
   * return the amount of ms to wait
   * @return the amount of ms to wait, or <=0 to not wait
   */
  protected int getWaitForStaggeredStartup() {
    // the amount of currently running runners
    long currentlyRunning = this.internalRunners.stream().filter(IIsolationData::isRunning).count();
    // as well as the amount of waiting-to-run runners
    currentlyRunning += this.staggerCount;
    if (currentlyRunning > 0) {
      long duration = this.staggeredStartUpFixedDelay + currentlyRunning * this.staggeredStartUpDelayPerRunner;
      // subtract the time which has passed between the last startup and now
      long diff = (this.lastStartup + duration) - System.currentTimeMillis();
      return (int) diff;
    }
    return -1;
  }

  /**
   * @return a set of packages which may be loaded from the context classloader
   * (and thus are not isolated)
   */
  protected Set<String> getPassThroughPackages() {
    return Set.of("org.gradle");
  }

  protected ClassLoader getClassLoader(URLClassLoader contextClassLoader, Supplier<T> supplier) {
    return new IsolatedURLClassLoader(contextClassLoader, getPassThroughPackages());
  }

  // Empty array of ProtectionDomain - see the doPrivileged() part below
  private static final ProtectionDomain[] NO_DOMAINS = new ProtectionDomain[0];

  // AccessControlContext without domains - see the doPrivileged() part below
  private static final AccessControlContext NO_DOMAINS_ACCESS_CONTROL_CONTEXT = new AccessControlContext(NO_DOMAINS);


  /**
   * Loads a class and runs a given method in an isolated class loader
   * Requires the class and method name as Strings,
   *  such that we do not load them from an existing parent classloader
   *
   * @param classname the name of a class {@link Class#getName()}
   * @param method    the method of a method {@link Method#getName()}
   * @param args      the arguments to a {@link de.monticore.gradle.common.AToolAction#doRun(String[])} method
   * @param prefix    Prefixes this string via a {@link PrefixStream}
   * @param predicate allows to check for additional elements, such as classpath
   * @param supplier  allows to set the additional arguments for a new loader
   */
  public void executeInClassloader(String classname, String method, String[] args,
                                   @Nullable String prefix,
                                   Predicate<T> predicate, Supplier<T> supplier) {
    final Thread currentThread = Thread.currentThread();
    ClassLoader originalClassLoader = currentThread.getContextClassLoader();
    try (IIsolationData<T> isolationData = getLoader(predicate, supplier)) {
      currentThread.setContextClassLoader(isolationData.getClassLoader());

      // set the prefix for the printing
      if (prefix != null) {
        redirectStream(getOrReplaceErr(), prefix);
        redirectStream(getOrReplaceOut(), prefix);
      }

      // Use doPrivileged() to avoid spawned threads inheriting the
      // AccessControlContext of the current thread.
      // No privileged actions are actually performed.
      // Reason: among the ProtectionDomains of the thread will otherwise be a
      // reference to the Isolated and/or children Groovy Classloader,
      // preventing the GC from reclaiming said Classloader with all its
      // loaded objects
      AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
        try{
          // Prepare for the cleanup after <a href="https://bugs.openjdk.org/browse/JDK-8078641">JDK-8078641</a>
          // Load the MethodHandleImpl class into the isolated classloader
          // as the GroovyInterpreter will otherwise load it within its
          // ClassLoaders, resulting in it being not accessible to this outer CL
          isolationData.getClassLoader().loadClass("java.lang.invoke.MethodHandleImpl");
        } catch (ReflectiveOperationException ignored) {}
        try {
          isolationData.getClassLoader().loadClass(classname)
                  .getMethod(method, String[].class)
                  .invoke(null, (Object) args);
        } catch (ReflectiveOperationException e) {
          passThrowableAlong(e.getCause());
        }
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
    }
  }

  /**
   * A {@link DomainCombiner} which removes inner class loaders from a Threads
   * {@link AccessControlContext}
   *
   * The {@link #isClassLoaderOrChild(ClassLoader)} method filters said
   * child class loaders.
   */
  protected DomainCombiner combiner = (currentDomains, assignedDomains) -> {
    // The assigned Domains should be equivalent to CachedIsolation.NO_DOMAINS or null
    if (assignedDomains != null && assignedDomains.length > 0)
      throw new IllegalStateException("Unexpected assignedDomains#length: " + assignedDomains.length);
    final List<ProtectionDomain> combinedWithoutIsolated = new ArrayList<>();
    for (ProtectionDomain protectionDomain : currentDomains) {
      if (protectionDomain.getClassLoader() == null
              || !isClassLoaderOrChild(protectionDomain.getClassLoader())) {
        combinedWithoutIsolated.add(protectionDomain);
      }
    }
    return combinedWithoutIsolated.toArray(new ProtectionDomain[0]);
  };

  /**
   * @return true if said class loader should not be included in an {@link AccessControlContext}
   */
  protected boolean isClassLoaderOrChild(ClassLoader classloader) {
    if (classloader == null) return false;
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


  /**
   * Close unused classloaders to free up memory
   */
  protected synchronized void cleanupOld() {
    long threshold = System.currentTimeMillis() - this.closeThreshold;
    Iterator<IIsolationData<T>> isolated = this.internalRunners.iterator();
    while (isolated.hasNext()) {
      IIsolationData<T> data = isolated.next();
      if (!data.isRunning() && data.getLastRun() < threshold){
        if (data.getClassLoader() instanceof Closeable) {
          // Close closeable classloaders
          try {
            ((Closeable) data.getClassLoader()).close();
          } catch (IOException ignored) { }
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

  protected static class IsolationData<T> implements IIsolationData<T> {
    protected ClassLoader classLoader;

    protected boolean running;

    protected long lastRun = System.currentTimeMillis();

    protected T extraData;

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
    public T getExtraData() {
      return extraData;
    }

    @Override
    public long getLastRun() {
      return lastRun;
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

  protected static class StaggeredIsolationData<T> implements IIsolationData<T> {
    protected Optional<IIsolationData<T>> actual = Optional.empty();
    protected final Supplier<IIsolationData<T>> supplier;

    public StaggeredIsolationData(Supplier<IIsolationData<T>> supplier) {
      this.supplier = supplier;
    }

    protected IIsolationData<T> getActual() {
      if (!actual.isPresent()) {
        actual = Optional.of(supplier.get());
      }
      return actual.get();
    }

    @Override
    public ClassLoader getClassLoader() {
      return getActual().getClassLoader();
    }

    @Override
    public boolean isRunning() {
      return getActual().isRunning();
    }

    @Override
    public void setRunning(boolean r) {
      getActual().setRunning(r);
    }

    @Override
    public T getExtraData() {
      return getActual().getExtraData();
    }

    @Override
    public void cleanUp() {
      getActual().cleanUp();
    }

    @Override
    public long getLastRun() {
      return getActual().getLastRun();
    }

    @Override
    public void close() {
      getActual().close();
    }
  }

  protected interface IIsolationData<T> extends AutoCloseable {
    ClassLoader getClassLoader();

    boolean isRunning();

    void setRunning(boolean r);

    T getExtraData();

    void cleanUp();

    long getLastRun();

    @Override
    void close();
  }

  /**
   * A {@link CachedIsolation} which just provides a set of isolated contexts
   */
  public static class Simple extends CachedIsolation<Boolean> {
    public void executeInClassloader(String classname, String method, String[] args, @Nullable String prefix) {
      super.executeInClassloader(classname, method, args, prefix, o -> true, () -> true);
    }
  }

  /**
   * A {@link CachedIsolation} which provides a set of isolated contexts
   *  with the ability to add extra elements to the classpath
   */
  public static class WithClassPath extends CachedIsolation<FileCollection> {
    public void executeInClassloader(String classname, String method, String[] args, @Nullable String prefix, FileCollection classPath) {
      super.executeInClassloader(classname, method, args, prefix,
              f -> f.minus(classPath).getFiles().isEmpty() && classPath.minus(f).getFiles().isEmpty(), // check if the difference between the classpaths is empty
              () -> classPath);
    }

    @Override
    protected ClassLoader getClassLoader(URLClassLoader contextClassLoader, Supplier<FileCollection> supplier) {
      // Merge the extra classpath with the current classpath
      URL[] urls = Stream.concat(
                      supplier.get().getFiles().stream().map(f -> {
                        try {
                          return f.toURI().toURL();
                        } catch (MalformedURLException e) {
                          throw new RuntimeException(e.getMessage(), e);
                        }
                      }), Arrays.stream(contextClassLoader.getURLs()))
              .toArray(URL[]::new);
      return new IsolatedURLClassLoader(urls, contextClassLoader, getPassThroughPackages());
    }
  }

}
