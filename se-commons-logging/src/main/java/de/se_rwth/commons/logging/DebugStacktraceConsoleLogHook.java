/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import de.se_rwth.commons.SourcePosition;

import java.io.PrintStream;

/**
 * log-hook printing all debug stacktraces to the console
 */
public class DebugStacktraceConsoleLogHook extends AStacktraceConsoleLogHook {
  public DebugStacktraceConsoleLogHook(PrintStream printStream) {
    super(printStream);
  }

  @Override
  public void doDebug(String msg, String logName) {
    printCurrentStackTrace(this.getPrintStream(), null);
  }

  @Override
  public void doDebug(String msg, SourcePosition pos, String logName) {
    printCurrentStackTrace(this.getPrintStream(), null);
  }

  @Override
  public void doDebug(String msg, SourcePosition start, SourcePosition end, String logName) {
    printCurrentStackTrace(this.getPrintStream(), null);
  }

  @Override
  public void doDebug(String msg, Throwable t, String logName) {
    printCurrentStackTrace(this.getPrintStream(), t);
  }

}
