/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.isolation;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

public class IsolatedURLClassLoader extends URLClassLoader {
  protected final Set<String> filteredPackages;
  protected final ClassLoader contextClassLoader;

  public IsolatedURLClassLoader(URLClassLoader contextClassLoader, Set<String> filteredPackages) {
    this(contextClassLoader.getURLs(), contextClassLoader, filteredPackages);
  }

  public IsolatedURLClassLoader(URL[] urls, URLClassLoader contextClassLoader, Set<String> filteredPackages) {
    super(urls, null);
    this.contextClassLoader = contextClassLoader;
    this.filteredPackages = filteredPackages;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    try {
      return super.findClass(name);
    } catch (ClassNotFoundException e) {
      if (filteredPackages.stream().anyMatch(name::startsWith))
        throw e;
      // Required to allow gradle transformer
      return this.contextClassLoader.loadClass(name);
    }
  }
}
