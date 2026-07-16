/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import java.io.PrintStream;

public abstract class AStacktraceConsoleLogHook implements ILogHook {

  protected PrintStream printStream;

  public AStacktraceConsoleLogHook(PrintStream printStream) {
    this.printStream = printStream;
  }

  protected PrintStream getPrintStream() {
    return printStream;
  }

  @Override
  public void doPrintln(String msg) {

  }

  @Override
  public void doErrPrint(String msg) {

  }

  @Override
  public void doPrintStackTrace(Throwable t) {

  }

  @Override
  public void doErrPrintStackTrace(Throwable t) {
  }

  @Override
  public void doPrint(String msg) {

  }


  protected void printStackTrace(PrintStream s, Throwable t) {
    StackTraceElement[] trace = new Throwable().getStackTrace();
    for (int i = 5; i < trace.length; i++) {
      s.println("\tat " + trace[i]);
    }
    if (t != null) {
      printStackTrace(s, t, "Caused by ", "");
    }
  }

  protected void printStackTrace(PrintStream s,
                                 Throwable throwable,
                                 String caption,
                                 String prefix) {
    StackTraceElement[] trace = throwable.getStackTrace();
    // Print header
    s.println(prefix + caption + throwable);
    // Print stacktrace
    for (StackTraceElement t : trace)
      s.println(prefix + "\tat " + t);
    // Do not print suppressed
    // Continue with caused by
    if (throwable.getCause() != null) {
      printStackTrace(s, throwable.getCause(), "Caused by ", prefix);
    }
  }

}
