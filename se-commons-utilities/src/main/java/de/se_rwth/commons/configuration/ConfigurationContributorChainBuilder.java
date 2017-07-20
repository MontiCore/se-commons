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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builds chain of responsibilities with {@link ConfigurationContributor}s as
 * chain elements.
 *
 */
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
