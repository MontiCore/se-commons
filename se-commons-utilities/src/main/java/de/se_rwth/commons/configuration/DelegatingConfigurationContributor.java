/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.configuration;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A simple delegating configuration contributor using a supplied delegate
 * configuration instance. Useful for reusing already existing configurations in
 * configuration chains.
 *
 */
@Deprecated
public final class DelegatingConfigurationContributor extends ConfigurationContributor {
  
  /* the configuration to delegate to */
  private final Configuration delegate;
  
  /* Creates a delegating configuration contributor with the supplied delegate
   * configuration. */
  DelegatingConfigurationContributor(Configuration delegate) {
    this.delegate = delegate;
  }
  
  /**
   * Creates a new delegating configuration contributor with the supplied
   * delegate target configuration.
   *
   * @param delegate to delegate to - must not be null
   * @return
   */
  public static DelegatingConfigurationContributor with(Configuration delegate) {
    return new DelegatingConfigurationContributor(delegate);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAllValues()
   */
  @Override
  public Map<String, Object> getAllValues() {
    return this.delegate.getAllValues();
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAllValuesAsStrings()
   */
  @Override
  public Map<String, String> getAllValuesAsStrings() {
    return this.delegate.getAllValuesAsStrings();
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsBoolean(java.lang.String)
   */
  @Override
  public Optional<Boolean> getAsBoolean(String key) {
    return this.delegate.getAsBoolean(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsBooleans(java.lang.String)
   */
  @Override
  public Optional<List<Boolean>> getAsBooleans(String key) {
    return this.delegate.getAsBooleans(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsDouble(java.lang.String)
   */
  @Override
  public Optional<Double> getAsDouble(String key) {
    return this.delegate.getAsDouble(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsDoubles(java.lang.String)
   */
  @Override
  public Optional<List<Double>> getAsDoubles(String key) {
    return this.delegate.getAsDoubles(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsInteger(java.lang.String)
   */
  @Override
  public Optional<Integer> getAsInteger(String key) {
    return this.delegate.getAsInteger(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsIntegers(java.lang.String)
   */
  @Override
  public Optional<List<Integer>> getAsIntegers(String key) {
    return this.delegate.getAsIntegers(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsString(java.lang.String)
   */
  @Override
  public Optional<String> getAsString(String key) {
    return this.delegate.getAsString(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getAsStrings(java.lang.String)
   */
  @Override
  public Optional<List<String>> getAsStrings(String key) {
    return this.delegate.getAsStrings(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getValue(java.lang.String)
   */
  @Override
  public Optional<Object> getValue(String key) {
    return this.delegate.getValue(key);
  }
  
  /**
   * @see de.se_rwth.commons.configuration.Configuration#getValues(java.lang.String)
   */
  @Override
  public Optional<List<Object>> getValues(String key) {
    return this.delegate.getValues(key);
  }

  /**
   * @see de.se_rwth.commons.configuration.Configuration#hasProperty(java.lang.String)
   */
  @Override
  public boolean hasProperty(String key) {
    return this.delegate.hasProperty(key);
  }
  
}
