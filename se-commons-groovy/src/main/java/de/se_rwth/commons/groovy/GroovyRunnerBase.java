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
package de.se_rwth.commons.groovy;

import de.se_rwth.commons.configuration.Configuration;
import de.se_rwth.commons.logging.Log;

/**
 * Base implementation for Groovy runners which (also) handles unchecked
 * exceptions (e.g. NPEs) properly.
 *
 */
public abstract class GroovyRunnerBase implements GroovyRunner {
  
  /**
   * @see de.se_rwth.commons.groovy.GroovyRunner#run(java.lang.String,
   * de.se_rwth.commons.configuration.Configuration)
   */
  @Override
  public final void run(String script, Configuration configuration) {
    try {
      doRun(script, configuration);
    }
    catch (Exception e) {
      Log.error("An internal error occured.", e);
    }
  }
  
  /**
   * Performs the actual execution of the given Groovy script.
   *
   * @param script to execute
   * @param configuration optional
   */
  protected abstract void doRun(String script, Configuration configuration);
  
}
