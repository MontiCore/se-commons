/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import java.util.ArrayList;
import java.util.List;

import de.se_rwth.commons.SourcePosition;

/**
 * This class is a stub for the centralized logging component.
 * It behaves exactly identical with the small difference that it does
 * not print anything to the console or any file.
 * It is thus free of sideffects and can be used for testing.
 *
 * To initialize it, run LogStub.init();
 *
 * The Log superclass already stores findings, so there is no need
 * to manage them here. We only need to remove the output
 *
 */
public class LogStub extends Log {
  
  protected LogStub() { }
  
  /**
   * Initialize the LogStub as Log
   */
  // Initialize the LogStub as Log
  public static void init() {
    Log.setLog(new LogStub());
  }
  
  /**
   * Check and terminate if required; i.e. if fail quick is enabled and error
   * count greater than zero.
   */
  protected final void terminateIfErrors() {
    // TODO: throw an appropriate unchecked Exception (that only is caught by JUnit)
    // does not terminate
  }
  
  /**
   * Print something
   */
  protected void doPrint(String msg) {
    // do nothing
  }

  /**
   * Print something on error channel
   */
  protected void doErrPrint(String msg) {
    // do nothing
  }

  /**
   * Print stacktrace of a throwable
   */
  protected void doPrintStackTrace(Throwable t) {
    // do nothing
  }

  /**
   * Print stacktrace of a throwable to error out
   */
  protected void doErrPrintStackTrace(Throwable t) {
    // do nothing
  }

/*
 * Please note: Methods like 
  
  // The customized behaviour 
  protected void doInfo(String msg, String logName) {   
    
  }

 * need not be changed, because they call the print methods
 * and it is sufficient to adapt these methods
 */

}
