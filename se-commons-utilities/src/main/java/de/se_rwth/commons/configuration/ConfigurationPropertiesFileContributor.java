/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
 * Copyright (c) 2017, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
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
