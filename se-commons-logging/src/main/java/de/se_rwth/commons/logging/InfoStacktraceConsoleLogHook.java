/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import java.io.PrintStream;

/**
 * log-hook printing all info stacktraces to the console
 */
public class InfoStacktraceConsoleLogHook extends AStacktraceConsoleLogHook {

  public InfoStacktraceConsoleLogHook(PrintStream printStream) {
    super(printStream);
  }

  @Override
  public void doInfo(String msg, String logName) {
    printCurrentStackTrace(this.getPrintStream(), null);
  }

  @Override
  public void doInfo(String msg, Throwable t, String logName) {
    printCurrentStackTrace(this.getPrintStream(), t);
  }
}
