/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.configuration;

import static com.google.common.collect.Iterables.transform;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

import de.se_rwth.commons.Splitters;
import de.se_rwth.commons.Values;

/**
 * A {@link ConfigurationContributor} that returns values from CLI arguments.
 *
 */
@Deprecated
public class ConfigurationPropertiesMapContributor extends ConfigurationContributor {
  
  /**
   * Factory method for {@link ConfigurationPropertiesMapContributor}.
   */
  public static final ConfigurationPropertiesMapContributor fromMap(Map<String, String> properties) {
    return fromMap(properties, Splitters.SEMICOLON);
  }
  
  /**
   * Factory method for {@link ConfigurationPropertiesMapContributor}.
   */
  public static final ConfigurationPropertiesMapContributor fromMap(
      Map<String, String> properties,
      Splitter splitter) {
    return new ConfigurationPropertiesMapContributor(properties, splitter);
  }
  
  /**
   * Factory method for {@link ConfigurationPropertiesMapContributor}.
   */
  public static final ConfigurationPropertiesMapContributor fromSplitMap(
      Multimap<String, String> properties) {
    return new ConfigurationPropertiesMapContributor(properties);
  }
  
  /**
   * Factory method for {@link ConfigurationPropertiesMapContributor}.
   */
  public static final ConfigurationPropertiesMapContributor fromSplitMap(
      Map<String, Iterable<String>> properties) {
    return new ConfigurationPropertiesMapContributor(properties);
  }
  
  private final Map<String, Iterable<String>> properties;
  
  /**
   * Constructor for
   * de.se_rwth.commons.configuration.ConfigurationPropertiesMapContributor
   */
  ConfigurationPropertiesMapContributor(Map<String, String> properties, Splitter listSplitter) {
    Splitter splitter = listSplitter.trimResults().omitEmptyStrings();
    Map<String, Iterable<String>> splitProperties = new HashMap<>();
    for (Map.Entry<String, String> property : properties.entrySet()) {
      splitProperties.put(property.getKey(), splitter.split(property.getValue()));
    }
    this.properties = splitProperties;
  }
  
  /**
   * Constructor for
   * de.se_rwth.commons.configuration.ConfigurationPropertiesMapContributor
   */
  public ConfigurationPropertiesMapContributor(Map<String, Iterable<String>> properties) {
    this.properties = Collections.unmodifiableMap(properties);
  }
  
  /**
   * Constructor for
   * de.se_rwth.commons.configuration.ConfigurationPropertiesMapContributor
   */
  ConfigurationPropertiesMapContributor(Multimap<String, String> properties) {
    Map<String, Iterable<String>> builtProperties = new HashMap<>();
    for (Map.Entry<String, String> property : properties.entries()) {
      builtProperties.put(property.getKey(), properties.get(property.getKey()));
    }
    this.properties = builtProperties;
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAllValues()
   */
  @Override
  public Map<String, Object> getAllValues() {
    return ImmutableMap.<String, Object> builder()
        .putAll(this.properties)
        .putAll(nextContributor().getAllValues())
        .build();
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAllValuesAsStrings()
   */
  @Override
  public Map<String, String> getAllValuesAsStrings() {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String> builder();
    this.properties.forEach((key, value) -> builder.put(key, value.toString()));
    builder.putAll(nextContributor().getAllValuesAsStrings());
    return builder.build();
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsBoolean(java.lang.String)
   */
  @Override
  public Optional<Boolean> getAsBoolean(String key) {
    String property = null;
    if (this.properties.containsKey(key)) {
      property = Iterables.getFirst(this.properties.get(key), null);
    }
    return property != null
        ? Optional.of(Boolean.valueOf(property))
        : nextContributor().getAsBoolean(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsBooleans(java.lang.String)
   */
  @Override
  public Optional<List<Boolean>> getAsBooleans(String key) {
    return this.properties.containsKey(key)
        ? Optional.<List<Boolean>> of(ImmutableList.<Boolean> copyOf(
            transform(this.properties.get(key),
                new Function<String, Boolean>() {
                  
                  @Override
                  public Boolean apply(String string) {
                    return Boolean.valueOf(string);
                  }
                })))
        : nextContributor().getAsBooleans(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsDouble(java.lang.String)
   */
  @Override
  public Optional<Double> getAsDouble(String key) {
    String property = null;
    if (this.properties.containsKey(key)) {
      property = Iterables.getFirst(this.properties.get(key), null);
    }
    return property != null
        ? Optional.of(Doubles.tryParse(property))
        : nextContributor().getAsDouble(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsDoubles(java.lang.String)
   */
  @Override
  public Optional<List<Double>> getAsDoubles(String key) {
    try {
      return this.properties.containsKey(key)
          ? Optional.<List<Double>> of(ImmutableList.<Double> copyOf(
              transform(this.properties.get(key),
                  new Function<String, Double>() {
                    
                    @Override
                    public Double apply(String string) {
                      return Doubles.tryParse(string);
                    }
                  })))
          : nextContributor().getAsDoubles(key);
    }
    catch (NumberFormatException e) {
      return Optional.empty();
    }
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsInteger(java.lang.String)
   */
  @Override
  public Optional<Integer> getAsInteger(String key) {
    String property = null;
    if (this.properties.containsKey(key)) {
      property = Iterables.getFirst(this.properties.get(key), null);
    }
    return property != null
        ? Optional.of(Ints.tryParse(property))
        : nextContributor().getAsInteger(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsIntegers(java.lang.String)
   */
  @Override
  public Optional<List<Integer>> getAsIntegers(String key) {
    try {
      return this.properties.containsKey(key)
          ? Optional.<List<Integer>> of(ImmutableList.<Integer> copyOf(
              transform(this.properties.get(key),
                  new Function<String, Integer>() {
                    
                    @Override
                    public Integer apply(String string) {
                      return Ints.tryParse(string);
                    }
                  })))
          : nextContributor().getAsIntegers(key);
    }
    catch (NumberFormatException e) {
      return Optional.empty();
    }
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsString(java.lang.String)
   */
  @Override
  public Optional<String> getAsString(String key) {
    String property = null;
    if (this.properties.containsKey(key)) {
      property = Iterables.getFirst(this.properties.get(key), null);
    }
    return property != null
        ? Optional.of(property)
        : nextContributor().getAsString(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsStrings(java.lang.String)
   */
  @Override
  public Optional<List<String>> getAsStrings(String key) {
    return this.properties.containsKey(key)
        ? Optional.<List<String>> of(ImmutableList.<String> copyOf(this.properties.get(key)))
        : nextContributor().getAsStrings(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getValue(java.lang.String)
   */
  @Override
  public Optional<Object> getValue(String key) {
    String property = null;
    if (this.properties.containsKey(key)) {
      property = Iterables.getFirst(this.properties.get(key), null);
    }
    return property != null
        ? Optional.<Object> of(property)
        : nextContributor().getValue(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getValues(java.lang.String)
   */
  @Override
  public Optional<List<Object>> getValues(String key) {
    return Values.checkTypes(this.properties.get(key), Object.class);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#hasProperty(java.lang.String)
   */
  @Override
  public boolean hasProperty(String key) {
    return this.properties.containsKey(key);
  }
  
}
