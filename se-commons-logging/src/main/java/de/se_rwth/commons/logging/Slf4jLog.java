/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

/**
 * The slf4j variant of {@link Log}. Use {@link Slf4jLog#init()} to enable
 * usage.
 *
 */
public class Slf4jLog {
  
  /**
   * Initializes slf4j backed logging as the centralized logging component.
   */
  @Deprecated
  public static void init() {
    replaceLogHooksWithSlf4j();
  }

  public static void replaceLogHooksWithSlf4j() {
    Log.getLog().logHooks.clear();
    Log.getLog().logHooks.add(new Slf4jLogHook());
    Log.log.isDEBUG = true;
    Log.log.isINFO = true;
    Log.log.isTRACE = true;
  }
  
  /* Utility class. */
  protected Slf4jLog() {
  }

}
