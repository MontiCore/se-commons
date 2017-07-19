/*
 * ******************************************************************************
 * MontiCore Language Workbench
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides configuration values of various types for given configuration keys.
 *
 */
public interface Configuration {
  
  /**
   * @return all values stored in this configuration.
   */
  Map<String, Object> getAllValues();
  
  /**
   * @return all values stored in this configuration as strings.
   */
  Map<String, String> getAllValuesAsStrings();
  
  /**
   * @return the value stored under the given key as Boolean.
   */
  Optional<Boolean> getAsBoolean(String key);
  
  /**
   * @return the value stored under the given key as Boolean.
   */
  Optional<List<Boolean>> getAsBooleans(String key);
  
  /**
   * @return the value stored under the given key as String.
   */
  Optional<Double> getAsDouble(String key);
  
  /**
   * @return the value stored under the given key as String.
   */
  Optional<List<Double>> getAsDoubles(String key);
  
  /**
   * @return the value stored under the given key as String.
   */
  Optional<Integer> getAsInteger(String key);
  
  /**
   * @return the value stored under the given key as String.
   */
  Optional<List<Integer>> getAsIntegers(String key);
  
  /**
   * @return the value stored under the given key as String.
   */
  Optional<String> getAsString(String key);
  
  /**
   * @return the value stored under the given key as String.
   */
  Optional<List<String>> getAsStrings(String key);
  
  /**
   * @return the value stored under the given key.
   */
  Optional<Object> getValue(String key);
  
  /**
   * @return the value stored under the given key.
   */
  Optional<List<Object>> getValues(String key);
  
  /**
   * @return whether this configuration contains the given key.
   */
  boolean hasProperty(String key);
  
}
