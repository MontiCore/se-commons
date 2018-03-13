/* (c) https://github.com/MontiCore/monticore */
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
