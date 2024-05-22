/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import de.se_rwth.commons.SourcePosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a centralized logging component. Subclasses may provide customized
 * implementations by setting itself as the singleton delegator target using
 * {@link Log#setLog(Log)}.<br>
 * If no other initialization is performed, then the Log is initialized
 * directly (@see Log#init())
 * <br>
 */
public class Log {

  // the single static delegator target
  protected static Log log;

  protected List<ILogHook> logHooks;

  protected IErrorHook errorHook;

  // Getter for the underlying Log. 
  protected static Log getLog() {
    ensureInitialization();
    return log;
  }

  /**
   * Ensure Log is initialized, if not make a local default initialization:
   * Initialize the Log directly as Log (INFO, WARN, ERRORs)
   * (and do not use a subclass like Slf4jLog)
   */
  public static void ensureInitialization() {
    if (log == null) {
      Log.init();
    }
  }

  /**
   * Initialize the Log directly as Log (INFO, WARN, ERRORs)
   * (and do not use a subclass like Slf4jLog)
   */
  public static void init() {
    Log l = new Log();
    l.isTRACE = false;
    l.isDEBUG = false;
    l.isINFO = true;
    l.logHooks = new ArrayList<>();
    l.logHooks.add(new ConsoleLogHook());
    l.errorHook = new DefaultErrorHook();
    Log.setLog(l);
  }

  /**
   * Initialize the Log directly as Log (also incl. DEBUG and TRACE infos)
   * (and do not use Slf4jLog)
   */
  public static void initDEBUG() {
    Log l = new Log();
    l.isDEBUG = true;
    l.isTRACE = true;
    l.isINFO = true;
    l.logHooks = new ArrayList<>();
    l.logHooks.add(new ConsoleLogHook());
    l.errorHook = new DefaultErrorHook();
    Log.setLog(l);
  }

  /**
   * Initialize the Log directly as Log (only WARN and ERROR)
   * (and do not use Slf4jLog)
   */
  public static void initWARN() {
    Log l = new Log();
    l.isTRACE = false;
    l.isDEBUG = false;
    l.isINFO = false;
    l.logHooks = new ArrayList<>();
    l.logHooks.add(new ConsoleLogHook());
    l.errorHook = new DefaultErrorHook();
    Log.setLog(l);
  }

  /**
   * Initialize the Log directly as Log (only TRACE)
   * (and do not use Slf4jLog)
   */
  public static void initTRACE() {
    Log l = new Log();
    l.isTRACE = true;
    l.isDEBUG = false;
    l.isINFO = false;
    l.logHooks = new ArrayList<>();
    l.logHooks.add(new ConsoleLogHook());
    l.errorHook = new DefaultErrorHook();
    Log.setLog(l);
  }

  /**
   * Initialize the Log as Slf4jLog
   * (which happens also if no explicit initialization is there)
   * TODO: remove Slf4j from dependencies and move this initialisation
   * into a subclass
   */
  @Deprecated
  public static void initSlf4j() {
    Slf4jLog.init();
  }

  /**
   * Initialize the Log as Log for interative systems (INFO, WARN,
   * (INTERNAL)ERROR, USER-ERROR)
   */
  public static void initInteractive() {
    Log l = new Log();
    l.isTRACE = false;
    l.isDEBUG = false;
    l.isINFO = true;
    l.isInteractive = true;
    l.logHooks = new ArrayList<>();
    l.logHooks.add(new ConsoleLogHook());
    Log.setLog(l);
  }

  /**
   * Assigns a new {@link Log} to use as central logging.
   *
   * @param log the new central logging to use; must not be null
   */
  // Allows to set an individually defined Log instance
  protected static final void setLog(Log log) {
    if (Log.log != null && log != null) {
      if (!Log.log.getClass().equals(log.getClass())) {
        debug("Switching logging component from " + Log.log.getClass().getName() + " to "
            + log.getClass().getName(), Log.class.getName());
      }
    }
    Log.log = log;
  }

  // terminate immediately on errors?
  protected boolean failQuick = true;

  // logging for interactive systems -> never terminates system
  protected boolean isInteractive = false;

  // show debugging and tracing info?
  protected boolean isDEBUG = false;
  protected boolean isTRACE = false;
  protected boolean isINFO = false;

  protected List<Finding> findings = new ArrayList<>();

  /* Utility class. */
  protected Log() {
    // TODO: Workaround by ALU to fix the NPE
    this.logHooks = new ArrayList<>();
    this.logHooks.add(new ConsoleLogHook());
    this.errorHook = new DefaultErrorHook();
    // TODO: END Workaround by ALU to fix the NPE
  }

  /**
   * Is level TRACE enabled for the given log name?
   *
   * @return whether level TRACE is enabled for the given log name
   */
  public static final boolean isTraceEnabled(String logName) {
    return getLog().doIsTraceEnabled(logName);
  }

  /**
   * Is level TRACE enabled for the given log name?
   *
   * @return whether level TRACE is enabled for the given log name
   */
  protected boolean doIsTraceEnabled(String logName) {
    return isTRACE;
  }

  /**
   * Log to the specified log name with level TRACE.
   *
   * @param msg     the trace message
   * @param logName the log name to use
   */
  public static final void trace(String msg, String logName) {
    getLog().doTrace(msg, logName);
  }

  /**
   * Log to the specified log with level TRACE.
   */
  protected void doTrace(String msg, String logName) {
    if (doIsTraceEnabled(logName)) {
      logHooks.forEach(hook -> hook.doTrace(msg, logName));
    }
  }

  /**
   * Log to the specified log name with level TRACE.
   *
   * @param msg     the trace message
   * @param t       the exception to log
   * @param logName the log name to use
   */
  public static final void trace(String msg, Throwable t, String logName) {
    getLog().doTrace(msg, t, logName);
  }

  /**
   * Log to the specified log with level TRACE.<br>
   */
  protected void doTrace(String msg, Throwable t, String logName) {
    if (doIsTraceEnabled(logName)) {
      trace(msg, logName);
      logHooks.forEach(hook -> hook.doTrace(msg, t, logName));
    }
  }

  /**
   * Is level DEBUG enabled for the given log name?
   *
   * @return whether level DEBUG is enabled for the given log name
   */
  public static final boolean isDebugEnabled(String logName) {
    return getLog().doIsDebugEnabled(logName);
  }

  /**
   * Is level DEBUG enabled for the given log name?
   *
   * @return whether level DEBUG is enabled for the given log name
   */
  protected boolean doIsDebugEnabled(String logName) {
    return isDEBUG;
  }

  /**
   * Log to the specified log name with level DEBUG.
   *
   * @param msg     the debug message
   * @param logName the log name to use
   */
  public static final void debug(String msg, String logName) {
    getLog().doDebug(msg, logName);
  }

  /**
   * Log to the specified log with level DEBUG.
   */
  protected void doDebug(String msg, String logName) {
    if (doIsDebugEnabled(logName)) {
      logHooks.forEach(hook -> hook.doDebug(msg, logName));
    }
  }

  public static final void debug(String msg, SourcePosition pos, String logName) {
    getLog().doDebug(msg, pos, logName);
  }

  protected void doDebug(String msg, SourcePosition pos, String logName) {
    if (doIsDebugEnabled(logName)) {
      logHooks.forEach(hook -> hook.doDebug(msg, pos, logName));
    }
  }

  public static final void debug(String msg, SourcePosition start, SourcePosition end, String logName) {
    getLog().doDebug(msg, start, end, logName);
  }

  protected void doDebug(String msg, SourcePosition start, SourcePosition end, String logName) {
    if (doIsDebugEnabled(logName)) {
      logHooks.forEach(hook -> hook.doDebug(msg, start, end, logName));
    }
  }

  /**
   * Log to the specified log name with level DEBUG.
   *
   * @param msg     the debug message
   * @param t       the exception to log
   * @param logName the log name to use
   */
  public static final void debug(String msg, Throwable t, String logName) {
    getLog().doDebug(msg, t, logName);
  }

  /**
   * Log to the specified log with level DEBUG.
   */
  protected void doDebug(String msg, Throwable t, String logName) {
    if (doIsDebugEnabled(logName)) {
      debug(msg, logName);
      logHooks.forEach(hook -> hook.doDebug(msg, t, logName));
    }
  }

  /**
   * Is level INFO enabled for the given log name?
   *
   * @return whether level INFO is enabled for the given log name
   */
  public static final boolean isInfoEnabled(String logName) {
    return getLog().doIsInfoEnabled(logName);
  }

  /**
   * Is level INFO enabled for the given log name?
   *
   * @return whether level INFO is enabled for the given log name
   */
  protected boolean doIsInfoEnabled(String logName) {
    return isINFO;
  }

  /**
   * Log to the specified log name with level INFO.
   *
   * @param msg     the info message
   * @param logName the log name to use
   */
  public static final void info(String msg, String logName) {
    getLog().doInfo(msg, logName);
  }

  /**
   * Log to the specified log with level INFO.
   */
  protected void doInfo(String msg, String logName) {
    if (doIsInfoEnabled(logName)) {
      logHooks.forEach(hook -> hook.doInfo(msg, logName));
    }
  }

  /**
   * Log to the specified log name with level INFO.
   *
   * @param msg     the info message
   * @param t       the exception to log
   * @param logName the log name to use
   */
  public static final void info(String msg, Throwable t, String logName) {
    getLog().doInfo(msg, t, logName);
  }

  /**
   * Log to the specified log with level INFO.
   */
  protected void doInfo(String msg, Throwable t, String logName) {
    if (doIsInfoEnabled(logName)) {
      info(msg, logName);
      logHooks.forEach(hook -> hook.doInfo(msg, t, logName));
    }
  }

  /**
   * Log with level WARN.
   *
   * @param msg the warn message
   */
  public static final void warn(String msg) {
    getLog().doWarn(msg);
  }

  /**
   * Log with level WARN. Intended for interactive systems.
   * Delegates to the default doWarn method.
   *
   * @param msg the warn message
   */
  public static final void warnUser(String msg) {
    getLog().doWarn(msg);
  }

  /**
   * Log with level WARN.
   */
  protected void doWarn(String msg) {
    Finding warn = Finding.warning(msg);
    addFinding(warn);
    logHooks.forEach(hook -> hook.doWarn(warn));
  }

  /**
   * Log with level WARN and source position.
   *
   * @param msg the warn message
   * @param pos the source position in a model file which caused the warning
   */
  public static final void warn(String msg, SourcePosition pos) {
    getLog().doWarn(msg, pos);
  }

  /**
   * Log with level WARN and source position.
   */
  protected void doWarn(String msg, SourcePosition pos) {
    Finding warn = Finding.warning(msg, pos);
    addFinding(warn);
    logHooks.forEach(hook -> hook.doWarn(warn));
  }

  /**
   * Log with level WARN and source position.
   *
   * @param msg   the error message
   * @param start the start position in a model file which caused the warning
   * @param end   the end position in a model file which caused the warning
   */
  public static final void warn(String msg, SourcePosition start, SourcePosition end) {
    getLog().doWarn(msg, start, end);
  }

  /**
   * Log with level ERROR and source position for start and end.
   */
  protected void doWarn(String msg, SourcePosition start, SourcePosition end) {
    Finding warn = Finding.warning(msg, start, end);
    addFinding(warn);
    logHooks.forEach(hook -> hook.doWarn(warn));
    terminateIfErrors();
  }

  /**
   * Log with level WARN.
   *
   * @param msg the warn message
   * @param t   the exception to log
   */
  public static final void warn(String msg, Throwable t) {
    getLog().doWarn(msg, t);
  }

  /**
   * Log with level WARN.
   */
  protected void doWarn(String msg, Throwable t) {
    Finding warn = Finding.warning(msg);
    addFinding(warn);
    logHooks.forEach(hook -> hook.doWarn(warn, t));
  }

  /**
   * Log with level ERROR.
   *
   * @param msg the error message
   */
  public static final void error(String msg) {
    getLog().doError(msg);
  }

  /**
   * Log with level ERROR.
   */
  protected void doError(String msg) {
    Finding error = Finding.error(msg);
    addFinding(error);
    logHooks.forEach(hook -> hook.doError(error));
    terminateIfErrors();
  }

  /**
   * Log with level ERROR and source position.
   *
   * @param msg the error message
   * @param pos the source position in a model file which caused the error
   */
  public static final void error(String msg, SourcePosition pos) {
    getLog().doError(msg, pos);
  }

  /**
   * Log with level ERROR and source position.
   */
  protected void doError(String msg, SourcePosition pos) {
    Finding error = Finding.error(msg, pos);
    addFinding(error);
    logHooks.forEach(hook -> hook.doError(error));
    terminateIfErrors();
  }

  /**
   * Log with level ERROR and source position.
   *
   * @param msg   the error message
   * @param start the start position in a model file which caused the error
   * @param end   the end position in a model file which caused the error
   */
  public static final void error(String msg, SourcePosition start, SourcePosition end) {
    getLog().doError(msg, start, end);
  }

  /**
   * Log with level ERROR and source position for start and end.
   */
  protected void doError(String msg, SourcePosition start, SourcePosition end) {
    Finding error = Finding.error(msg, start, end);
    addFinding(error);
    logHooks.forEach(hook -> hook.doError(error));
    terminateIfErrors();
  }

  /**
   * Log with level ERROR.
   *
   * @param msg the error message
   * @param t   the exception to log
   */
  public static final void error(String msg, Throwable t) {
    getLog().doError(msg, t);
  }

  /**
   * Log with level ERROR.
   */
  protected void doError(String msg, Throwable t) {
    Finding error = Finding.error(msg);
    addFinding(error);
    logHooks.forEach(hook -> hook.doError(error, t));
    terminateIfErrors();
  }

  /**
   * Log with level ERROR. Intended for interactive systems.
   * Delegates to the default doError method.
   *
   * @param msg the error message
   */
  public static final void errorInternal(String msg) {
    getLog().doError(msg);
  }

  /**
   * Log with level ERROR and source position.
   * Intended for interactive systems.
   * Delegates to the default doError method.
   *
   * @param msg the error message
   * @param pos the source position in a model file which caused the error
   */
  public static final void errorInternal(String msg, SourcePosition pos) {
    getLog().doError(msg, pos);
  }

  /**
   * Log with level ERROR and source position.
   * Intended for interactive systems.
   * Delegates to the default doError method.
   *
   * @param msg   the error message
   * @param start the start position in a model file which caused the error
   * @param end   the end position in a model file which caused the error
   */
  public static final void errorInternal(String msg, SourcePosition start, SourcePosition end) {
    getLog().doError(msg, start, end);
  }

  /**
   * Log with level ERROR.
   * Intended for interactive systems.
   * Delegates to the default doError method.
   *
   * @param msg the error message
   * @param t   the exception to log
   */
  public static final void errorInternal(String msg, Throwable t) {
    getLog().doError(msg, t);
  }

  /**
   * Log with level ERROR. Intended for interactive systems.
   * Delegates to a custom doUserError method, which provides
   * custom behavior not affecting the overall system.
   * Only available in interactive mode. Otherwise, uses default logging.
   *
   * @param msg the error message
   */
  public static final void errorUser(String msg) {
    if (isInteractive()) {
      getLog().doErrorUser(msg);
    } else {
      error(msg);
    }
  }

  /**
   * Log with level ERROR and source position.
   * Delegates to a custom doUserError method, which provides
   * custom behavior not affecting the overall system.
   * Only available in interactive mode. Otherwise, uses default logging.
   *
   * @param msg the error message
   * @param pos the source position in a model file which caused the error
   */
  public static final void errorUser(String msg, SourcePosition pos) {
    if (isInteractive()) {
      getLog().doErrorUser(msg, pos);
    } else {
      error(msg, pos);
    }
  }

  /**
   * Log with level ERROR. Intended for interactive systems.
   * Delegates to a custom doUserError method, which provides
   * custom behavior not affecting the overall system.
   * Only available in interactive mode. Otherwise, uses default logging.
   *
   * @param msg   the error message
   * @param start the start position in a model file which caused the error
   * @param end   the end position in a model file which caused the error
   */
  public static final void errorUser(String msg, SourcePosition start, SourcePosition end) {
    if (isInteractive()) {
      getLog().doErrorUser(msg, start, end);
    } else {
      error(msg, start, end);
    }
  }

  /**
   * Log with level ERROR. Intended for interactive systems.
   * Delegates to a custom doUserError method, which provides
   * custom behavior not affecting the overall system.
   * Only available in interactive mode. Otherwise, uses default logging.
   *
   * @param msg the error message
   * @param t   the exception to log
   */
  public static final void errorUser(String msg, Throwable t) {
    if (isInteractive()) {
      getLog().doErrorUser(msg, t);
    } else {
      error(msg, t);
    }
  }

  /**
   * Log with level ERROR. For user errors in interactive systems.
   */
  protected void doErrorUser(String msg) {
    Finding error = Finding.userError(msg);
    addFinding(error);
    logHooks.forEach(hook -> hook.doErrorUser(error));
  }

  /**
   * Log with level ERROR and source position. For user errors in interactive systems.
   */
  protected void doErrorUser(String msg, SourcePosition pos) {
    Finding error = Finding.userError(msg, pos);
    addFinding(error);
    logHooks.forEach(hook -> hook.doErrorUser(error));
  }

  /**
   * Log with level ERROR and source position for start and end. For user errors in interactive systems.
   */
  protected void doErrorUser(String msg, SourcePosition start, SourcePosition end) {
    Finding error = Finding.userError(msg, start, end);
    addFinding(error);
    logHooks.forEach(hook -> hook.doErrorUser(error));
  }

  /**
   * Log with level ERROR. For user errors in interactive systems.
   */
  protected void doErrorUser(String msg, Throwable t) {
    Finding error = Finding.userError(msg);
    addFinding(error);
    logHooks.forEach(hook -> hook.doErrorUser(error, t));
  }

  /**
   * Checks whether the given reference is a null reference.
   *
   * @param reference to check
   * @param message   in case the reference is in fact null
   * @return the reference or throws a {@link NullPointerException} with the
   * given message
   */
  public static final <T> T errorIfNull(T reference, String message) {
    if (reference == null) {
      throw new NullPointerException(message);
    }
    return reference;
  }

  /**
   * Checks whether the given reference is a null reference.
   *
   * @param reference to check
   * @return the reference or throws a {@link NullPointerException} with a
   * default message
   */
  public static final <T> T errorIfNull(T reference) {
    return errorIfNull(reference,
        "Internal error: a null reference occurred (see/enable debug output).");
  }

  /**
   * Enables/disables fail quick mechanism. If fail quick is enabled (default)
   * each invocation of an error log should terminate the application. While
   * fail quick is disabled, the amount of error logs is stored and can be
   * accessed by {@link Log#getErrorCount()}. Also if fail quick is re-enabled,
   * the application is terminated if error logs occurred during the disabled
   * period.
   *
   * @param enable or disable fail quick; the application terminates if error
   *               logs occurred during a previous disabled period
   */
  public static final void enableFailQuick(boolean enable) {
    getLog().doEnableFailQuick(enable);
  }

  /**
   * Enables/disables fail quick mechanism.
   */
  protected void doEnableFailQuick(boolean enable) {
    this.failQuick = enable;
    terminateIfErrors();
  }

  /**
   * Enables/disables interactive mode. If interactive mode is disnabled
   * (default) each invocation of an error log should terminate the
   * application. While interactive mode is enabled, the amount of error logs
   * is stored and can be accessed by {@link Log#getErrorCount()}.
   * Also, the application distinguishes between user errors (expected
   * system behavior) and internal errors (unexpected system behavior).
   *
   * @param enable or disable interactive mode
   */
  public static final void enableInteractive(boolean enable) {
    getLog().doEnableInteractive(enable);
  }

  /**
   * Enables/disables interactive mode.
   */
  protected void doEnableInteractive(boolean enable) {
    this.isInteractive = enable;
  }

  /**
   * Is fail quick enabled?
   *
   * @return whether fail quick is enabled
   */
  public static final boolean isInteractive() {
    return getLog().doIsInteractive();
  }

  /**
   * Is fail quick enabled?
   */
  protected boolean doIsInteractive() {
    return this.isInteractive;
  }

  /**
   * Return the amount of errors which occurred.
   *
   * @return
   */
  public static final long getErrorCount() {
    return getLog().doGetErrorCount();
  }

  /**
   * Return the amount of errors which occurred.
   */
  protected long doGetErrorCount() {
    return this.findings.stream().filter(f -> f.isError()).count();
  }

  /**
   * Return the amount of Findings which occurred.
   *
   * @return No. of Findings (errors and warnings)
   */
  public static final long getFindingsCount() {
    return getLog().doGetFindingsCount();
  }

  /**
   * Return the amount of Findings which occurred.
   */
  protected long doGetFindingsCount() {
    return this.findings.size();
  }

  /**
   * Is fail quick enabled?
   *
   * @return whether fail quick is enabled
   */
  public static final boolean isFailQuickEnabled() {
    return getLog().doIsFailQuickEnabled();
  }

  /**
   * Is fail quick enabled?
   */
  protected boolean doIsFailQuickEnabled() {
    return this.failQuick;
  }

  /**
   * Increment the error log counter.
   */
  protected void addFinding(Finding finding) {
    this.findings.add(finding);
  }

  /**
   * Check and terminate if required; i.e. if fail quick is enabled and error count greater than zero.
   */
  protected void terminateIfErrors() {
    // do not exit if:
    // 1. fail quick is disabled
    // 2. no error occured
    // 3. running in interactive mode
    if (isFailQuickEnabled() && getErrorCount() > 0 && !isInteractive()) {
      this.errorHook.terminate();
    }
  }

  /**
   * Get the list of all previously occurred findings.
   *
   * @return list of all findings
   */
  public static List<Finding> getFindings() {
    return getLog().doGetFindings();
  }

  /**
   * Return the findings.
   */
  protected List<Finding> doGetFindings() {
    return this.findings;
  }

  /**
   * Clear the list of all previously occurred findings
   * (e.g. after each test).
   */
  public static void clearFindings() {
    getLog().doClearFindings();
  }

  /**
   * Clear the findings.
   */
  protected void doClearFindings() {
    findings.clear();
  }

  /**
   * Reset List of prints
   */
  public static void printFindings() {
    for (int i = 0; i < getFindings().size(); i++) {
      System.out.printf("#%d : %s\n", i, getFindings().get(i));
    }
  }

  /**
   * Print something on System.out
   */
  public static void print(String msg) {
    getLog().doPrint(msg);
  }

  protected void doPrint(String msg) {
    logHooks.forEach(hook -> hook.doPrint(msg));
  }

  /**
   * Print something on System.out
   */
  public static void println(String msg) {
    getLog().doPrintln(msg);
  }

  protected void doPrintln(String msg) {
    logHooks.forEach(hook -> hook.doPrintln(msg));
  }

  /**
   * Print stacktrace of a throwable
   * (to be adapted in subclasses)
   */
  protected void doPrintStackTrace(Throwable t) {
    logHooks.forEach(hook -> hook.doPrintStackTrace(t));
  }

  protected void doErrPrintStackTrace(Throwable t) {
    logHooks.forEach(hook -> hook.doErrPrintStackTrace(t));
  }

  public static void addLogHook(ILogHook hook) {
    getLog().logHooks.add(hook);
  }

  public static void removeLogHook(ILogHook hook) {
    getLog().logHooks.remove(hook);
  }

  public static void setErrorHook(IErrorHook hook) {
    getLog().errorHook = hook;
  }
}
