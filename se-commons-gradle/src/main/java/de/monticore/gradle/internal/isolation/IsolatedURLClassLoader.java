/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.isolation;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

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

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
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
    // the groovy ClassInfo causes the garbage collector to not be able to
    // collect loaded classes, causing a memory leak
    if (this.findLoadedClass("org.codehaus.groovy.reflection.ClassInfo") != null) {
      try {
        loadClass("de.monticore.gradle.internal.isolation.GroovyLeakCleanup")
                .getMethod("cleanUp").invoke(null);
      } catch (ReflectiveOperationException ignored) { }
    }
    super.close();
  }

}
