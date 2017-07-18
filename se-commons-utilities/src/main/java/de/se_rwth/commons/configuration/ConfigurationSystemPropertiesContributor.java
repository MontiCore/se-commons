/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
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

import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;

import de.se_rwth.commons.Splitters;

/**
 * A {@link ConfigurationContributor} that returns values from system
 * properties.
 *
 */
public final class ConfigurationSystemPropertiesContributor extends
    ConfigurationPropertiesMapContributor {
  
  /**
   * Factory method for {@link ConfigurationSystemPropertiesContributor}.
   */
  public static final ConfigurationSystemPropertiesContributor withPrefix(String prefix) {
    return new ConfigurationSystemPropertiesContributor(prefix);
  }
  
  /**
   * Constructor for
   * de.se_rwth.commons.configuration.ConfigurationSystemPropertiesContributor
   */
  ConfigurationSystemPropertiesContributor(final String prefix) {
    super(new ForwardingMap<String, String>() {
      
      private final Map<String, String> properties = Maps.fromProperties(System.getProperties());
      
      private final String prefixWithDot = prefix.endsWith(".") ? prefix : prefix + ".";
      
      @Override
      public String get(@Nullable Object key) {
        return key != null ? super.get(this.prefixWithDot + key) : super.get(key);
      }
      
      @Override
      protected Map<String, String> delegate() {
        return this.properties;
      }
    }, Splitters.SEMICOLON);
  }
  
}
