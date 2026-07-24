/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import java.io.PrintStream;

/**
 * Abstract superclass for log hooks that print the location of the finding
 * to their printstream.
 * Does nothing by itself.
 * Implementing subclasses may want to call {@link #printCurrentStackTrace(PrintStream, Throwable)}
 */
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

  /**
   * How many stack frames are between the printCurrentStackTrace call
   * and the initial Log report call.
   */
  protected static int STACKTRACE_OFFSET_TO_LOG_CALL = 5;

  /**
   * Print the stacktrace to the Log call.
   * Optionally also prints the causing throwable.
   *
   * @param s the target print stream
   * @param t an optional throwable to print as the cause
   */
  protected void printCurrentStackTrace(PrintStream s, Throwable t) {
    StackTraceElement[] trace = new Throwable().getStackTrace();
    // Print the stacktrace of the log call
    for (int i = STACKTRACE_OFFSET_TO_LOG_CALL; i < trace.length; i++) {
      s.println("\tat " + trace[i]);
    }
    if (t != null) {
      // Also print the throwable if present
      continueToPrintStackTrace(s, t, "Caused by ", "");
    }
  }

  /**
   * Print a throwable with a caption (such as 'Caused by ')
   *
   * @param s         the target printstream
   * @param throwable the throwable
   * @param caption   the caption at the header
   * @param prefix    a prefix printed at the start of each line
   */
  protected void continueToPrintStackTrace(PrintStream s,
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
      continueToPrintStackTrace(s, throwable.getCause(), "Caused by ", prefix);
    }
  }

}
