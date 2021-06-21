/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.configuration;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builds chain of responsibilities with {@link ConfigurationContributor}s as
 * chain elements.
 *
 */
@Deprecated
public final class ConfigurationContributorChainBuilder {
  
  /**
   * Factory method for {@link ConfigurationContributorChainBuilder}.
   */
  public static ConfigurationContributorChainBuilder newChain() {
    return new ConfigurationContributorChainBuilder();
  }
  
  private final EmptyConfigurationContributor emptyContributor = EmptyConfigurationContributor.SINGLETON;
  
  public ConfigurationContributor first;
  
  public ConfigurationContributor last;
  
  /**
   * Adds a new {@link ConfigurationContributor} to the end of the chain.
   */
  public ConfigurationContributorChainBuilder add(ConfigurationContributor contributor) {
    checkNotNull(contributor);
    if (this.first == null) {
      this.first = contributor;
      this.last = contributor;
    }
    else {
      this.last.setNextContributor(contributor);
      this.last = contributor;
    }
    return this;
  }
  
  /**
   * @return the first element of the built chain.
   */
  public ConfigurationContributor build() {
    return this.first != null
        ? this.first
        : this.emptyContributor;
  }
  
}
