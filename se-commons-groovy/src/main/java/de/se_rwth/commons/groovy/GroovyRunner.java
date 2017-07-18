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

/**
 * A simple interface denoting classes which can run Groovy scripts with an
 * optionally supplied {@link Configuration} instance to pull parameters/options
 * from to be passed to the script.
 *
 * @since 1.7.0
 */
public interface GroovyRunner {
  
  /**
   * Executes the specified/supplied Groovy script and makes all
   * options/parameters supplied by the optional {@link Configuration} instance
   * available to the script.
   *
   * @param configuration - an optional instance of {@link Configuration}
   * containing configuration options/parameter to supply to the script to
   * execute
   * @param script - the Groovy script to execute; <b>NOTE:</b> this interface
   * makes no assumptions whether this argument contains the script source
   * itself or the path to a resource which contains the Groovy script;
   * implementations of this signature are expected to document what exactly is
   * expected in the implementation
   */
  void run(String script, Configuration configuration);
}
