/* (c) https://github.com/MontiCore/monticore */
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
