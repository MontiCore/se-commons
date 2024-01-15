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

  protected final List<IsolationData<T>> internalRunners = Collections.synchronizedList(new LinkedList<>());

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
   * a new instance is created.
   */
  protected synchronized IsolationData<T> getLoader(Predicate<T> predicate, Supplier<T> supplier) {
    Optional<IsolationData<T>> d = this.internalRunners.stream()
            .filter(x -> !x.running)
            .filter(x -> predicate.test(x.extraData))
            .findAny();
    if (d.isPresent()) {
      d.get().running = true;
      return d.get();
    }
    this.cleanupOld();
    IsolationData<T> data = new IsolationData<>();
    data.classLoader = getClassLoader((URLClassLoader) Thread.currentThread().getContextClassLoader(), supplier);
    data.running = true;
    data.extraData = supplier.get();
    this.internalRunners.add(data);
    setupTimer();
    return data;
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
    ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    try (IsolationData<T> isolationData = getLoader(predicate, supplier)) {
      Thread.currentThread().setContextClassLoader(isolationData.classLoader);

      // set the prefix for the printing
      if (prefix != null) {
        redirectStream(getOrReplaceErr(), prefix);
        redirectStream(getOrReplaceOut(), prefix);
      }

      isolationData.classLoader.loadClass(classname)
              .getMethod(method, String[].class)
              .invoke(null, (Object) args);
    } catch (ReflectiveOperationException e) {
      passThrowableAlong(e.getCause());
    } finally {
      if (prefix != null) {
        // Reset this threads prefix printers
        getOrReplaceErr().reset();
        getOrReplaceOut().reset();
      }
      Thread.currentThread().setContextClassLoader(originalClassLoader);
    }
  }

  protected String getMessage(Throwable throwable) {
    if (throwable.getMessage() != null)
      return throwable.getMessage();
    if (throwable.getMessage() == null && throwable.getCause() != null)
      return getMessage(throwable.getCause());
    return "no exc";
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
    if (System.err instanceof PrintStreamThreadProxy)
      return (PrintStreamThreadProxy) System.err;
    System.setErr(new PrintStreamThreadProxy(System.err));
    return (PrintStreamThreadProxy) System.err;
  }

  /**
   * See {@link PrintStreamThreadProxy}
   *
   * @return the wrapped error stream
   */
  protected synchronized PrintStreamThreadProxy getOrReplaceOut() {
    if (System.out instanceof PrintStreamThreadProxy)
      return (PrintStreamThreadProxy) System.out;
    System.setOut(new PrintStreamThreadProxy(System.out));
    return (PrintStreamThreadProxy) System.out;
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
    Iterator<IsolationData<T>> isolated = this.internalRunners.iterator();
    while (isolated.hasNext()) {
      IsolationData<T> data = isolated.next();
      if (!data.running && data.lastRun < threshold){
        if (data.classLoader instanceof Closeable) {
          // Close closeable classloaders
          try {
            ((Closeable) data.classLoader).close();
          } catch (IOException ignored) { }
        }
        data.classLoader = null;
        data.extraData = null;
        isolated.remove();
      }
    }
    if (cleanupTimer != null && this.internalRunners.isEmpty()) {
      cleanupTimer.cancel();
      cleanupTimer = null;
    }
  }

  protected static class IsolationData<T> implements AutoCloseable {
    ClassLoader classLoader;
    boolean running;

    long lastRun = System.currentTimeMillis();

    T extraData;

    @Override
    public void close() {
      this.running = false;
      this.lastRun = System.currentTimeMillis();
    }
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
