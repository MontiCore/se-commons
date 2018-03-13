/* (c) https://github.com/MontiCore/monticore */
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
