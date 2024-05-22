/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import java.util.ArrayList;
import java.util.List;

public class LogPrintCollector implements ILogHook {

  /**
   * List of prints
   */
  protected List<String> prints = new ArrayList<>();

  /**
   * Reset List of prints
   */
  public void clearPrints() {
    prints = new ArrayList<>();
  }

  /**
   * Get List of prints
   */
  public List<String> getPrints() {
    return prints;
  }

  /**
   * Print something
   */
  public void doPrint(String msg) {
    prints.add(msg);
  }

  /**
   * Print something
   */
  public void doPrintln(String msg) {
    prints.add(msg + System.lineSeparator());
  }

  /**
   * Print something on error channel
   */
  public void doErrPrint(String msg) {
    prints.add("Error: " + msg);
  }

  /**
   * Print stacktrace of a throwable
   */
  public void doPrintStackTrace(Throwable t) {
    prints.add("Stacktrace of " + t.getClass() + " object");
  }

  /**
   * Print stacktrace of a throwable to error out
   */
  public void doErrPrintStackTrace(Throwable t) {
    prints.add("Error: Stacktrace of " + t.getClass() + " object");
  }


}
