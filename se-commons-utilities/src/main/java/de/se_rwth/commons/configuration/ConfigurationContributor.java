/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.configuration;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A part of a chain of {@link Configuration}s.
 *
 */
@Deprecated
public abstract class ConfigurationContributor implements Configuration {
  
  private final EmptyConfigurationContributor emptyContributor = EmptyConfigurationContributor.SINGLETON;
  
  private ConfigurationContributor nextContributor = null;
  
  /**
   * @return the next {@link ConfigurationContributor} in the chain or an
   * {@link EmptyConfigurationContributor} if this is the last element of the
   * chain.
   */
  protected final ConfigurationContributor nextContributor() {
    return this.nextContributor != null ? this.nextContributor : this.emptyContributor;
  }
  
  /**
   * Sets the next contributor in the chain.
   */
  final void setNextContributor(ConfigurationContributor nextContributor) {
    checkState(this.nextContributor == null);
    this.nextContributor = checkNotNull(nextContributor);
  }
  
}
