/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import de.se_rwth.commons.Splitters;

/**
 * A {@link ConfigurationContributor} that returns values from a properties
 * file.
 *
 */
@Deprecated
public final class ConfigurationPropertiesFileContributor extends
    ConfigurationPropertiesMapContributor {
  
  /**
   * SLF4J logger
   */
  private static final Logger logger = LoggerFactory
      .getLogger(ConfigurationPropertiesFileContributor.class);
  
  /**
   * Factory method for {@link ConfigurationPropertiesFileContributor}.
   */
  public static final ConfigurationPropertiesFileContributor fromPropertiesFile(File file) {
    Properties properties = new Properties();
    try {
      properties.load(Files.newReader(file, Charsets.UTF_8));
    }
    catch (FileNotFoundException e) {
      logger.error("Unable to load properties from {}", file);
    }
    catch (IOException e) {
      logger.error("Unable to load properties from {}", file);
    }
    return new ConfigurationPropertiesFileContributor(properties);
  }
  
  /**
   * Constructor for
   * de.se_rwth.commons.configuration.ConfigurationPropertiesFileContributor
   */
  ConfigurationPropertiesFileContributor(Properties properties) {
    super(Maps.fromProperties(properties), Splitters.COMMA);
  }
  
}
