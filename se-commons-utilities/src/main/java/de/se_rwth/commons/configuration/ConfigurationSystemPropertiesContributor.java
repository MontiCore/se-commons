/* (c) https://github.com/MontiCore/monticore */
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
