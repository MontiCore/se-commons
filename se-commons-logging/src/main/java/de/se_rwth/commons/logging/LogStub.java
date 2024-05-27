/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is a stub for the centralized logging component.
 * It behaves exactly identical with the small difference that it does
 * not print anything to the console or any file.
 * It is thus free of sideffects and can be used for testing.
 * <p>
 * To initialize it, run LogStub.init();
 * <p>
 * The Log superclass already stores findings, so there is no need
 * to manage them here. We only need to keep the output locally
 */
public class LogStub extends Log {

  public static LogPrintCollector collector;

  protected LogStub() {
    // TODO: Workaround to fix the NPE
    this.collector = new LogPrintCollector();
    this.logHooks = new ArrayList<>();
    this.logHooks.add(collector);
    this.errorHook = new DefaultErrorHook();

  }

  // LogStub can in addition behave like Log (and.e.g output results)
  boolean alsoBehavesLikeLog = false;

  /**
   * Initialize the Log directly as Log (INFO, WARN, ERROR)
   * (and do not use a subclass like Slf4jLog)
   */
  public static void ensureInitialization() {
    if (log == null) {
      LogStub.init();
    }
  }

  /**
   * Initialize the LogStub as Log
   * WARN andERROR are internally memorized as findings.
   * INFO and prints are memorized in the prints' List.
   */
  public static void init() {
    LogStub l = new LogStub();
    collector = new LogPrintCollector();
    l.isTRACE = false;
    l.isDEBUG = false;
    l.isINFO = true;
    l.logHooks = new ArrayList<>();
    l.logHooks.add(collector);
    l.errorHook = new DefaultErrorHook();
    Log.setLog(l);
  }

  /**
   * Initialize the LogStub as Log + original Log-Behavior
   * (like init(), but adds behavior as normal log)
   */
  public static void initPlusLog() {
    init();
    Log.getLog().logHooks.add(new ConsoleLogHook());
  }

  /**
   * Check and terminate if required; i.e. if fail quick is enabled and error
   * count greater than zero.
   */
  protected final void terminateIfErrors() {
    // TODO: throw an appropriate unchecked Exception (that only is caught by JUnit)
    // does not terminate
  }

/*
 * Please note: Methods like
  protected void doInfo(String msg, String logName) {  ...  }
 * need not be changed, because they call the print methods
 * and it is sufficient to adapt these methods
 */

  /**
   * Log to the specified log with level INFO.
   */
  // The customized behaviour
  protected void doInfo(String msg, String logName) {
    // adapted implementation
    super.doInfo(msg, logName);
  }

  /**
   * Get List of prints
   */
  public static List<String> getPrints() {
    return collector.getPrints();
  }

  /**
   * Reset List of prints
   */
  public static void clearPrints() {
    if (collector != null) {
      collector.clearPrints();
    }
   }

  /**
   * prints out all log messages to the console
   */
  public static void printPrints() {
    for(int i = 0; i < collector.prints.size(); i++) {
      System.out.printf("#%d : %s\n", i, collector.prints.get(i));
    }
  }

}
