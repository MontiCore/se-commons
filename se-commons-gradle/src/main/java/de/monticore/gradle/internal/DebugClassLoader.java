/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal;

import de.monticore.gradle.common.CommonMCTask;
import org.gradle.api.file.ConfigurableFileCollection;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.function.Consumer;

public class DebugClassLoader {

  /**
   * Executes a Consumer with extra classpath elements
   *
   * Does NOT isolate, thus useful when debugging
   *
   * @param extraClasspathElements {@link CommonMCTask#getExtraClasspathElements()}
   * @param runMethod {@link CommonMCTask#getRunMethod()}
   * @param args the main arguments
   */
  public static void runDirectWithExtraClassPath(ConfigurableFileCollection extraClasspathElements, Consumer<String[]> runMethod, String[] args) {
    // Turn the extra classpath elements to an array of URLs
    URL[] urls = extraClasspathElements.getFiles().stream().map(f -> {
      try {
        return f.toURI().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }).toArray(URL[]::new);
    // backup the current context classloader
    ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();

    // We explicitly do not set the parent classloader to null,
    // thus not isolating this urlClassLoader from the context
    try (URLClassLoader urlClassLoader = new URLClassLoader(urls)) {
      Thread.currentThread().setContextClassLoader(urlClassLoader);
      runMethod.accept(args);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      // reset the context classloader
      Thread.currentThread().setContextClassLoader(currentThreadClassLoader);
    }
  }
}
