/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import java.io.PrintStream;

/**
 * log-hook printing all warning stacktraces to the console
 */
public class WarningStacktraceConsoleLogHook extends AStacktraceConsoleLogHook {
  public WarningStacktraceConsoleLogHook(PrintStream printStream) {
    super(printStream);
  }

  @Override
  public void doWarn(Finding warn) {
    printCurrentStackTrace(this.getPrintStream(), null);
  }

  @Override
  public void doWarn(Finding warn, Throwable t) {
    printCurrentStackTrace(this.getPrintStream(), t);
  }
}
