/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import de.se_rwth.commons.SourcePosition;

/**
 * A log hook handles printing messages and stacktrace in the case of an error
 * or a warning, e.g. by printing them out directly to the console or to a file.
 * <p>
 * Implementations of this interface can be added and removed to the log voa
 * {@link Log#addLogHook(ILogHook)} and {@link Log#removeLogHook(ILogHook)},
 * respectively.
 */
public interface ILogHook {

  default void doTrace(String msg, String logName) {
    doPrintln(String.format("[TRACE]  %s %s", logName, msg));
  }

  default void doTrace(String msg, Throwable t, String logName) {
    doTrace(msg, logName);
    doPrintStackTrace(t);
  }

  default void doDebug(String msg, String logName) {
    doPrintln(String.format("[DEBUG]  %s %s", logName, msg));
  }

  default void doDebug(String msg, SourcePosition pos, String logName) {
    doPrintln(String.format("[DEBUG]  %s %s:%s", logName, pos.toString(), msg));
  }

  default void doDebug(String msg,
                       SourcePosition start,
                       SourcePosition end,
                       String logName) {
    doPrintln(String.format("[DEBUG]  %s %s - %s:%s",
      logName, start.toString(), end.toString(), msg)
    );
  }

  default void doDebug(String msg, Throwable t, String logName) {
    doDebug(msg, logName);
    doPrintStackTrace(t);
  }

  default void doInfo(String msg, String logName) {
    doPrintln(formatInfo(msg, logName));
  }

  default void doInfo(String msg, Throwable t, String logName) {
    doInfo(msg, logName);
    doPrintStackTrace(t);
  }

  default String formatInfo(String msg, String logName) {
    return String.format("[INFO]  %s %s", logName, msg);
  }

  default void doWarn(Finding warn) {
    doPrintln(formatWarn(warn));
  }

  default void doWarn(Finding warn, Throwable t) {
    doPrintln(formatWarn(warn));
    doPrintStackTrace(t);
  }

  default String formatWarn(Finding warn) {
    return String.format("[WARN]  %s", warn);
  }

  default void doError(Finding error) {
    doPrintln(formatError(error));
  }

  default void doError(Finding error, Throwable t) {
    doPrintln(formatError(error));
    doErrPrintStackTrace(t);
  }

  default String formatError(Finding error) {
    return String.format("[ERROR]  %s", error);
  }

  default void doErrorUser(Finding error) {
    doPrintln(formatErrorUser(error));
  }

  default void doErrorUser(Finding error, Throwable t) {
    doPrintln(formatErrorUser(error));
    doErrPrintStackTrace(t);
  }

  default String formatErrorUser(Finding error) {
    return String.format("[USER-ERROR]  %s", error);
  }

  void doPrintln(String msg);

  void doErrPrint(String msg);

  void doPrintStackTrace(Throwable t);

  void doErrPrintStackTrace(Throwable t);

  void doPrint(String msg);

}
