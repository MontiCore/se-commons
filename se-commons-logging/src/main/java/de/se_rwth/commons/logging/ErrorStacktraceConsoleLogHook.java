/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import java.io.PrintStream;

/**
 * log-hook printing all error and user error stacktraces to the console
 */
public class ErrorStacktraceConsoleLogHook extends AStacktraceConsoleLogHook {
  public ErrorStacktraceConsoleLogHook(PrintStream printStream) {
    super(printStream);
  }

  @Override
  public void doErrorUser(Finding error, Throwable t) {
    printCurrentStackTrace(this.getPrintStream(), t);
  }

  @Override
  public void doErrorUser(Finding error) {
    printCurrentStackTrace(this.getPrintStream(), null);
  }

  @Override
  public void doError(Finding error, Throwable t) {
    printCurrentStackTrace(this.getPrintStream(), t);
  }

  @Override
  public void doError(Finding error) {
    printCurrentStackTrace(this.getPrintStream(), null);
  }
}
