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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A part of a chain of {@link Configuration}s.
 * 
 * @author (last commit) $Author$
 * @version $Revision$, $Date$
 */
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
