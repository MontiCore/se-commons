/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import java.io.PrintStream;

/**
 * log-hook printing all trace stacktraces to the console
 */
public class TraceStacktraceConsoleLogHook extends AStacktraceConsoleLogHook {
  public TraceStacktraceConsoleLogHook(PrintStream printStream) {
    super(printStream);
  }


  @Override
  public void doTrace(String msg, String logName) {
    printCurrentStackTrace(this.getPrintStream(), null);
  }

  @Override
  public void doTrace(String msg, Throwable t, String logName) {
    printCurrentStackTrace(this.getPrintStream(), t);
  }
}
