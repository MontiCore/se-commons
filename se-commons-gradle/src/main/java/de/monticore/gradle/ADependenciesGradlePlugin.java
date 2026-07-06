/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * Shared methods for all dependencies plugins
 */
public abstract class ADependenciesGradlePlugin implements Plugin<Project> {

  /**
   * Loads the key "version" from the classpath's buildInfo.properties
   *
   * @return the version key
   */
  protected String getToolVersion() {
    Properties properties = Objects.requireNonNull(loadProperties("buildInfo.properties"), "Failed to load buildInfo.properties");
    return Objects.requireNonNull(properties.getProperty("version"), "Failed to read key version from buildInfo.properties");
  }

  /**
   * Loads a properties file from the buildInfo.properties from the classpath
   *
   * @return the properties
   */
  public Properties loadProperties() {
    return this.loadProperties("buildInfo.properties");
  }

  /**
   * Loads a properties file from the classpath's resources
   *
   * @param filename the filename
   * @return the properties
   */
  public Properties loadProperties(String filename) {
    Properties properties = new Properties();
    try {
      properties.load(this.getClass().getClassLoader().getResourceAsStream(filename));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return properties;
  }
}
