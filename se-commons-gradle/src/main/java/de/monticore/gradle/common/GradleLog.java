/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.common;

import de.se_rwth.commons.logging.ConsoleLogHook;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;

public class GradleLog extends Log {

  /**
   * Initialize the GradleLog directly as GradleLog (INFO, WARN, ERROR)
   */
  public static void init() {
    GradleLog l = new GradleLog();
    l.isTRACE = false;
    l.isDEBUG = false;
    l.isINFO = true;
    l.logHooks = new ArrayList<>();
    l.logHooks.add(new ConsoleLogHook());
    l.errorHook = new GradleErrorHook();
    GradleLog.setLog(l);
  }

  /**
   * Initialize the GradleLog directly as GradleLog (TRACE, DEBUG, INFO, WARN, ERROR)
   */
  public static void initDEBUG() {
    GradleLog l = new GradleLog();
    l.isDEBUG = true;
    l.isTRACE = true;
    l.isINFO = true;
    l.logHooks = new ArrayList<>();
    l.logHooks.add(new ConsoleLogHook());
    l.errorHook = new GradleErrorHook();
    GradleLog.setLog(l);
  }

  /**
   * Initialize the GradleLog directly as GradleLog (WARN, ERROR)
   */
  public static void initWARN() {
    GradleLog l = new GradleLog();
    l.isTRACE = false;
    l.isDEBUG = false;
    l.isINFO = false;
    l.logHooks = new ArrayList<>();
    l.logHooks.add(new ConsoleLogHook());
    l.errorHook = new GradleErrorHook();
    GradleLog.setLog(l);
  }

  /**
   * Initialize the GradleLog directly as GradleLog (only TRACE, WARN, ERROR)
   */
  public static void initTRACE() {
    GradleLog l = new GradleLog();
    l.isTRACE = true;
    l.isDEBUG = false;
    l.isINFO = false;
    l.logHooks = new ArrayList<>();
    l.logHooks.add(new ConsoleLogHook());
    l.errorHook = new GradleErrorHook();
    GradleLog.setLog(l);
  }
}
