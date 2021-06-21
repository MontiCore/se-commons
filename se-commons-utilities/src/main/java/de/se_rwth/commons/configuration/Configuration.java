/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.configuration;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import jline.internal.Log;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides configuration values of various types for given configuration keys.
 *
 */
public interface Configuration {
  
  /**
   * @return all values stored in this configuration.
   */
  default Map<String, Object> getAllValues() {
    Map<String, Object> values = new HashMap<>();
    for (Option option : getConfig().getOptions()) {
      if (option.getArgs() == 1) {
        values.put(option.getOpt(), option.getValue());
      } else {
        values.put(option.getOpt(), option.getValuesList());
      }
    }
    return values;
  }
  
  /**
   * @return all values stored in this configuration as strings.
   */
  default Map<String, String> getAllValuesAsStrings() {
    Map<String, String> values = new HashMap<>();
    for (Option option : getConfig().getOptions()) {
      if (option.getArgs() == 1) {
        values.put(option.getOpt(), option.getValue());
      } else {
        values.put(option.getOpt(), option.getValuesList().toString());
      }
    }
    return values;
  }
  
  /**
   * @return the value stored under the given key as Boolean.
   */
  default Optional<Boolean> getAsBoolean(String key) {
    String property = null;
    if (getConfig().hasOption(key)) {
      property = getConfig().getOptionValue(key);
    }
    return property != null
            ? Optional.ofNullable(Boolean.valueOf(property))
            : Optional.empty();
  }
  
  /**
   * @return the value stored under the given key as Boolean.
   */
  default Optional<List<Boolean>> getAsBooleans(String key) {
    List<String> properties = new ArrayList<>();
    if (getConfig().hasOption(key)) {
      properties = Arrays.asList(getConfig().getOptionValues(key));
    }

    return Optional.ofNullable(properties.stream()
            .map(p -> Boolean.valueOf(p))
            .collect(Collectors.toList()));
  }
  
  /**
   * @return the value stored under the given key as String.
   */
  default Optional<Double> getAsDouble(String key) {
    String property = null;
    if (getConfig().hasOption(key)) {
      property = getConfig().getOptionValue(key);
    }
    return property != null
            ? Optional.ofNullable(Doubles.tryParse(property))
            : Optional.empty();
  }
  
  /**
   * @return the value stored under the given key as String.
   */
  default Optional<List<Double>> getAsDoubles(String key) {
    List<String> properties = new ArrayList<>();
    if (getConfig().hasOption(key)) {
      properties = Arrays.asList(getConfig().getOptionValues(key));
    }

    return Optional.ofNullable(properties.stream()
            .map(p -> Doubles.tryParse(p))
            .collect(Collectors.toList()));
  }
  
  /**
   * @return the value stored under the given key as String.
   */
  default Optional<Integer> getAsInteger(String key) {
    String property = null;
    if (getConfig().hasOption(key)) {
      property = getConfig().getOptionValue(key);
    }

    return property != null
            ? Optional.of(Ints.tryParse(property))
            : Optional.empty();
  }
  
  /**
   * @return the value stored under the given key as String.
   */
  default Optional<List<Integer>> getAsIntegers(String key) {
    List<String> properties = new ArrayList<>();
    if (getConfig().hasOption(key)) {
      properties = Arrays.asList(getConfig().getOptionValues(key));
    }

    return Optional.ofNullable(properties.stream()
            .map(p -> Ints.tryParse(p))
            .collect(Collectors.toList()));
  }
  
  /**
   * @return the value stored under the given key as String.
   */
  default Optional<String> getAsString(String key) {
    if (getConfig().hasOption(key)) {
      return Optional.ofNullable(getConfig().getOptionValue(key));
    }
    return Optional.empty();
  }
  
  /**
   * @return the value stored under the given key as String.
   */
  default Optional<List<String>> getAsStrings(String key) {
    if (getConfig().hasOption(key)) {
      return Optional.ofNullable(Arrays.asList(getConfig().getOptionValues(key)));
    }
    return Optional.empty();
  }
  
  /**
   * @return the value stored under the given key.
   */
  default Optional<Object> getValue(String key) {
    if (getConfig().hasOption(key)) {
      return Optional.ofNullable(getConfig().getOptionValue(key));
    }
    return Optional.empty();
  }
  
  /**
   * @return the value stored under the given key.
   */
  default Optional<List<Object>> getValues(String key) {
    if (getConfig().hasOption(key)) {
      return Optional.ofNullable(Arrays.asList(getConfig().getOptionValues(key)));
    }
    return Optional.empty();
  }
  
  /**
   * @return whether this configuration contains the given key.
   */
  default boolean hasProperty(String key) {
    return getConfig().hasOption(key);
  }

  /**
   * @return the command line API object containing the configuration
   */
  default CommandLine getConfig() {
    Log.error("0xA1010 The getter for the configuration is not implemented. " +
            "You might want to implement a wrapper class to allow getting this value.");
    return null;
  }
}
