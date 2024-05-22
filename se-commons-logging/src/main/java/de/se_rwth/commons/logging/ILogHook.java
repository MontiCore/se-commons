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
    doPrintln(String.format("[TRACE] %s %s", logName, msg));
  }

  default void doTrace(String msg, Throwable t, String logName) {
    doTrace(msg, logName);
    doPrintStackTrace(t);
  }

  default void doDebug(String msg, String logName) {
    doPrintln(String.format("[DEBUG] %s %s", logName, msg));
  }

  default void doDebug(String msg, SourcePosition pos, String logName) {
    doPrintln(String.format("[DEBUG] %s %s:%s", logName, pos.toString(), msg));
  }

  default void doDebug(String msg,
                       SourcePosition start,
                       SourcePosition end,
                       String logName) {
    doPrintln(String.format("[DEBUG] %s %s - %s:%s",
      logName, start.toString(), end.toString(), msg)
    );
  }

  default void doDebug(String msg, Throwable t, String logName) {
    doDebug(msg, logName);
    doPrintStackTrace(t);
  }

  default void doInfo(String msg, String logName) {
    doPrintln(String.format("[INFO]  %s %s", logName, msg));
  }

  default void doInfo(String msg, Throwable t, String logName) {
    doInfo(msg, logName);
    doPrintStackTrace(t);
  }

  default void doWarn(Finding warn) {
    doPrintln(String.format("[WARN] %s", warn));
  }

  default void doWarn(Finding warn, Throwable t) {
    doPrintln(String.format("[WARN]  %s", warn));
    doPrintStackTrace(t);
  }

  default void doError(Finding error) {
    doPrintln(String.format("[ERROR]  %s", error));
  }

  default void doError(Finding error, Throwable t) {
    doPrintln(String.format("[ERROR]  %s", error));
    doErrPrintStackTrace(t);
  }

  default void doErrorUser(Finding error) {
    doPrintln(String.format("[USER-ERROR]  %s", error));
  }

  default void doErrorUser(Finding error, Throwable t) {
    doPrintln(String.format("[USER-ERROR]  %s", error));
    doErrPrintStackTrace(t);
  }

  void doPrintln(String msg);

  void doErrPrint(String msg);

  void doPrintStackTrace(Throwable t);

  void doErrPrintStackTrace(Throwable t);

  void doPrint(String msg);

}
