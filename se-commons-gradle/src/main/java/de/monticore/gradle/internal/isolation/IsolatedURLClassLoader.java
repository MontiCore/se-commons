/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.isolation;

import com.google.common.collect.Iterables;
import de.se_rwth.commons.io.CleanerProvider;
import de.se_rwth.commons.io.SyncDeIsolated;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class IsolatedURLClassLoader extends URLClassLoader {
  protected final Set<String> passThroughPackages;
  protected final ClassLoader contextClassLoader;

  public IsolatedURLClassLoader(URLClassLoader contextClassLoader, Set<String> passThroughPackages) {
    this(contextClassLoader.getURLs(), contextClassLoader, passThroughPackages);
  }

  public IsolatedURLClassLoader(URL[] urls, URLClassLoader contextClassLoader, Set<String> passThroughPackages) {
    super(urls, null);
    this.contextClassLoader = contextClassLoader;
    this.passThroughPackages = passThroughPackages;
  }

  private static final String CLEANER_PROVIDER_NAME = CleanerProvider.class.getName();
  private static final String SYNCDEISOLATED_NAME = SyncDeIsolated.class.getName();

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    // We explicitly do not isolate some classes:
    if (name.equals(CLEANER_PROVIDER_NAME) // Tracks usages across isolates instances
        || name.equals(SYNCDEISOLATED_NAME) // Allows synchronized mutex locks between isolated instances
        || name.startsWith("org.slf4j")) // also pass slf4j through (to allow gradle to handle logging)
    {
      return this.contextClassLoader.loadClass(name);
    }
    try {
      return super.findClass(name);
    } catch (ClassNotFoundException e) {
      if (passThroughPackages.stream().noneMatch(name::startsWith))
        throw e;
      // Required to allow gradle transformers
      return this.contextClassLoader.loadClass(name);
    }
  }

  @Override
  public void close() throws IOException {
    // Call all shutdown hooks created within this class loader:
    Iterable<Thread> shutdownHooks = this.getShutdownHooks();
    // Next, call all (runtime) shutdown hooks (of this classloader)
    // If we were not to trigger/remove them, this classloader is never able to be GC
    List<Thread> threadsToWaitOn = new ArrayList<>();

    for (Thread thread : shutdownHooks) {
      if (Runtime.getRuntime().removeShutdownHook(thread)) {
        // Remove the shutdown hook from the runtime
        thread.start(); // and start the thread/run the hook
        threadsToWaitOn.add(thread);
      }
    }

    // Next, wait for all these recently started threads to finish
    for (Thread thread : threadsToWaitOn) {
      while (true) {
        try {
          thread.join();
          break;
        } catch (InterruptedException ignored) {
        }
      }
    }
    // And finally, make *really* sure we unset their context classloader
    for (Thread thread : Iterables.concat(Thread.getAllStackTraces().keySet(), shutdownHooks)) {
      if (thread.getContextClassLoader() != IsolatedURLClassLoader.this)
        continue; // but only threads within this context
      thread.setContextClassLoader(null);
    }

    // Next, apply various cleanup operations
    // the groovy ClassInfo causes the garbage collector to not be able to
    // collect loaded classes, causing a memory leak
    if (this.findLoadedClass("org.codehaus.groovy.reflection.ClassInfo") != null) {
      runCleanUp("de.monticore.gradle.internal.isolation.GroovyLeakCleanup");
    }
    // TODO: Remove once we switch to java 18 or higher
    // Clean up after JDK-8078641 with child classloaders
    runCleanUp("de.monticore.gradle.internal.isolation.JDK8078641Cleanup");

    super.close();
  }

  /**
   * Run additional cleanup operations.
   * After a cleanup is run, no guarantee on the state of the classes of this
   * classloader is given, i.e. non-null objects may be null, etc.
   * The order in which cleanups are performed might be relevant due to this.
   * DO NOT CALL THIS METHOD UNLESS YOU ARE ALREADY CLOSING YOUR CLASSLOADER
   */
  protected void runCleanUp(String cleanupClass) {
    try {
      loadClass(cleanupClass).getMethod("cleanUp").invoke(null);
    } catch (Throwable ignored) {
      // ignore failed cleanup
      // We explicitly catch all Throwables, including errors here,
      // as NoClassDefFoundErrors are possible
    }
  }

  /**
   * Fetch all ApplicationShutdownHooks
   */
  protected Iterable<Thread> getShutdownHooks() {
    List<Thread> ret = new ArrayList<>();
    try {
      // Note: Thread.getAllStackTraces().keySet() is not guaranteed to contain shutdown hooks
      // which is the reason we access the ApplicationShutdownHooks via reflections
      Class<?> hooksClass = Class.forName("java.lang.ApplicationShutdownHooks");
      Field hooksField = hooksClass.getDeclaredField("hooks");
      hooksField.setAccessible(true);
      Map<Thread, Thread> actualHooks = (Map<Thread, Thread>) hooksField.get(null);
      for (Thread thread : actualHooks.keySet()) {
        if (thread.getContextClassLoader() == this) {
          ret.add(thread);
        }
      }

    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to fetch shutdown hook threads", e);
    }
    return ret;
  }

}
