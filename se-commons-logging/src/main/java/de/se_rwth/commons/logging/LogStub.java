/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This class is a stub for the centralized logging component.
 * It behaves exactly identical with the small difference that it does
 * not print anything to the console or any file.
 * It is thus free of sideffects and can be used for testing.
 *
 * To initialize it, run LogStub.init();
 *
 * The Log superclass already stores findings, so there is no need
 * to manage them here. We only need to keep the output locally
 *
 */
public class LogStub extends Log {
  
  protected LogStub() { }
  
  // LogStub can in addition behave like Log (and.e.g output results)
  boolean alsoBehavesLikeLog = false;
  
  /**
   * Initialize the Log directly as Log (INFO, WARN, ERRORs)
   * (and do not use a subclass like Slf4jLog)
   */
  public static void ensureInitalization() {
    if (log == null) {
      LogStub.init();
    }
  }
  
  /**
   * Initialize the LogStub as Log
   * (WARN, ERRORs are internally memorized as findings)
   * (INFO and prints are memorized in the prints'List
   */
  public static LogStub init() {
    LogStub l = new LogStub();
    l.isTRACE = false;
    l.isDEBUG = false;
    l.isINFO  = true;
    l.isNonZeroExit = false;
    l.alsoBehavesLikeLog = false;
    Log.setLog(l);
    return l;
  }
  
  /**
   * Initialize the LogStub as Log + original Log-Behavior
   * (like init(), but adds behavior as normal log)
   */
  public static LogStub initPlusLog() {
    LogStub l = LogStub.init();
    l.alsoBehavesLikeLog = false;
    return l;
  }
  
  /**
   * Check and terminate if required; i.e. if fail quick is enabled and error
   * count greater than zero.
   */
  protected final void terminateIfErrors() {
    // TODO: throw an appropriate unchecked Exception (that only is caught by JUnit)
    // does not terminate
  }
  
  // ---------------------------------------------------
  // Handle the output
  
  /**
   * List of prints
   */
  protected static List<String> prints = new ArrayList<>();
  
  /**
   * Reset List of prints
   */
  public static void clearPrints() {
    prints = new ArrayList<>();
  }
  
  /**
   * Get List of prints
   */
  public static List<String> getPrints() {
    return prints;
  }
  
  /**
   * Reset List of prints
   */
  public static void printPrints() {
    for(int i = 0; i < prints.size(); i++) {
      System.out.printf("#%d : %s\n", i, prints.get(i));
    }
  }
  
  /**
   * Print something
   */
  protected void doPrint(String msg) {
    prints.add(msg);
  }

  /**
   * Print something
   */
  protected void doPrintln(String msg) {
    prints.add(msg+"\n");
  }

  /**
   * Print something on error channel
   */
  protected void doErrPrint(String msg) {
    prints.add("Error: "+msg);
  }

  /**
   * Print stacktrace of a throwable
   */
  protected void doPrintStackTrace(Throwable t) {
    prints.add("Stacktrace of "+t.getClass()+" object");
  }

  /**
   * Print stacktrace of a throwable to error out
   */
  protected void doErrPrintStackTrace(Throwable t) {
    prints.add("Error: Stacktrace of "+t.getClass()+" object");
  }

/*
 * Please note: Methods like
  protected void doInfo(String msg, String logName) {  ...  }
 * need not be changed, because they call the print methods
 * and it is sufficient to adapt these methods
 */

}
