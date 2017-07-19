/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2017, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
package de.se_rwth.commons.logging;

import java.util.ArrayList;
import java.util.List;

import de.se_rwth.commons.SourcePosition;

/**
 * Provides a centralized logging component. Subclasses may provide customized
 * implementations by setting itself as the singleton delegator target using
 * {@link Log#setLog(Log)}.<br>
 * If no other initialization is performed {@link Slf4jLog} will be used by
 * default.<br>
 * <br>
 * <b>Please note:</b> hands-on usage is demonstrated in {@link LogTest}.
 */
public class Log {
  
  // the single static delegator target
  protected static Log log;

  // Getter for the underlying Log. By default Slf4jLog
  protected static Log getLog() {
    if (log == null) {
      Slf4jLog.init();

    }
    return log;
  }

  /**
   * Initialize the Log directly as Log (INFo, WARN, ERRORs)
   * (an do not use Slf4jLog)
   */
  public static void init() {
    Log l = new Log();
    l.isTRACE = false;
    l.isDEBUG = false;
    Log.setLog(l);
  }

  /**
   * Initialize the Log directly as Log (incl. DEBUG and TRACE infos)
   * (an do not use Slf4jLog)
   */
  public static void initDEBUG() {
    Log.setLog(new Log());
  }

  /**
   * Initialize the Log directly as Log (only WARN and ERROR)
   * (an do not use Slf4jLog)
   */
  public static void initWARN() {
    Log l = new Log();
    l.isTRACE = false;
    l.isDEBUG = false;
    l.isINFO  = false;
    Log.setLog(l);
  }

  /**
   * Initialize the Log as Slf4jLog
   * (which happens also if no explicit initialization is there)
   * TODO: rem Slf4j from dependencies and move this initialisation 
   * into a subclass
   */
  public static void initSlf4j() {
    Slf4jLog.init();
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
  boolean failQuick = true;
  
  // terminate with an non-zero exit code
  boolean isNonZeroExit = false;

  // show debugging and tracing info?
  boolean isDEBUG = true;
  boolean isTRACE = true;
  boolean isINFO  = true;
  
  List<Finding> findings = new ArrayList<>();
  
  /* Utility class. */
  protected Log() {
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
   * @param msg the trace message
   * @param logName the log name to use
   */
  public static final void trace(String msg, String logName) {
    getLog().doTrace(msg, logName);
  }
  
  /**
   * Log to the specified log with level TRACE.
   */
  protected void doTrace(String msg, String logName) {
    if(doIsTraceEnabled(logName)) {
      doPrint("[TRACE] " + logName + " " + msg);
    }
  }
  
  /**
   * Log to the specified log name with level TRACE.
   * 
   * @param msg the trace message
   * @param t the exception to log
   * @param logName the log name to use
   */
  public static final void trace(String msg, Throwable t, String logName) {
    getLog().doTrace(msg, t, logName);
  }
  
  /**
   * Log to the specified log with level TRACE.<br>
   */
  protected void doTrace(String msg, Throwable t, String logName) {
    if(doIsTraceEnabled(logName)) {
      trace(msg, logName);
      doPrintStackTrace(t);
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
   * @param msg the debug message
   * @param logName the log name to use
   */
  public static final void debug(String msg, String logName) {
    getLog().doDebug(msg, logName);
  }
  
  /**
   * Log to the specified log with level DEBUG.
   */
  protected void doDebug(String msg, String logName) {
    if(doIsDebugEnabled(logName)) {
      doPrint("[DEBUG] " + logName + " " + msg);
    }
  }
  
  /**
   * Log to the specified log name with level DEBUG.
   * 
   * @param msg the debug message
   * @param t the exception to log
   * @param logName the log name to use
   */
  public static final void debug(String msg, Throwable t, String logName) {
    getLog().doDebug(msg, t, logName);
  }
  
  /**
   * Log to the specified log with level DEBUG.
   */
  protected void doDebug(String msg, Throwable t, String logName) {
    if(doIsDebugEnabled(logName)) {
      debug(msg, logName);
      doPrintStackTrace(t);
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
   * @param msg the info message
   * @param logName the log name to use
   */
  public static final void info(String msg, String logName) {
    getLog().doInfo(msg, logName);
  }
  
  /**
   * Log to the specified log with level INFO.
   */
  protected void doInfo(String msg, String logName) {
    if(doIsInfoEnabled(logName)) {
      doPrint("[INFO]  " + logName + " " + msg);
    }

  }
  
  /**
   * Log to the specified log name with level INFO.
   * 
   * @param msg the info message
   * @param t the exception to log
   * @param logName the log name to use
   */
  public static final void info(String msg, Throwable t, String logName) {
    getLog().doInfo(msg, t, logName);
  }
  
  /**
   * Log to the specified log with level INFO.
   */
  protected void doInfo(String msg, Throwable t, String logName) {
    if(doIsInfoEnabled(logName)) {
      info(msg, logName);
      doPrintStackTrace(t);
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
   * Log with level WARN.
   */
  protected void doWarn(String msg) {
    Finding warn = Finding.warning(msg);
    addFinding(warn);
    doPrint("[WARN]  " + warn.toString());
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
    doPrint("[WARN]  " + warn.toString());
  }
  
  /**
   * Log with level WARN.
   * 
   * @param msg the warn message
   * @param t the exception to log
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
    doPrint("[WARN]  " + warn.toString());
    doPrintStackTrace(t);
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
    doErrPrint("[ERROR] " + error.toString());
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
    doErrPrint("[ERROR] " + error.toString());
    terminateIfErrors();
  }
  
  /**
   * Log with level ERROR.
   * 
   * @param msg the error message
   * @param t the exception to log
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
    doErrPrint("[ERROR] " + error.toString());
    doErrPrintStackTrace(t);
    terminateIfErrors();
  }
  
  /**
   * Checks whether the given reference is a null reference.
   * 
   * @param reference to check
   * @param message in case the reference is in fact null
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
        "Internal error: a null reference occured (see/enable debug output).");
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
   * logs occurred during a previous disabled period
   */
  public static final void enableFailQuick(boolean enable) {
    getLog().doEnableFailQuick(enable);
  }
  
  /**
   * Enables/disables terminating with non-zero exit code
   */
  public static final void enableNonZeroExit(boolean enable) {
    getLog().doEnableNonZeroExit(enable);
  }
  
  /**
   * Enables/disables fail quick mechanism.
   */
  protected void doEnableFailQuick(boolean enable) {
    this.failQuick = enable;
    terminateIfErrors();
  }
  
  /**
   * Enables/disables terminating with non-zero exit code
   */
  protected void doEnableNonZeroExit(boolean enable) {
    this.isNonZeroExit = enable;
    terminateWithNonZeroCodeIfErrors();
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
   * Is exit with non-zero code enabled?
   * 
   * @return whether non-zero exit is enabled
   */
  public static final boolean isNonZeroExitEnabled() {
    return getLog().doIsNonZeroExitEnabled();
  }
  
  /**
   * Is exit with non-zero code enabled?
   */
  protected boolean doIsNonZeroExitEnabled() {
    return this.isNonZeroExit;
  }
  
  /**
   * Increment the error log counter.
   */
  protected void addFinding(Finding finding) {
    this.findings.add(finding);
  }
  
  /**
   * Check and terminate if required; i.e. if fail quick or non-zero exit code
   * are enabled enabled and error count greater than zero.
   */
  protected void terminateIfErrors() {
    if (getErrorCount() > 0) {
      // FIXME: replace with runtime exception and appropriate catch block at
      // top level; System.exit() is a smell/bad depending on the context
      if (isNonZeroExitEnabled()) {
        System.exit(1);
      }
      if (isFailQuickEnabled()) {
        System.exit(0);
      }
    }
  }
  
  /**
   * Check and terminate with exit code 1 if required; i.e. if non-zero exit is enabled and error
   * count greater than zero.
   */
  protected void terminateWithNonZeroCodeIfErrors() {
    // FIXME: replace with runtime exception and appropriate catch block at top
    // level; System.exit() is a smell/bad depending on the context
    if (isNonZeroExitEnabled() && getErrorCount() > 0) {
      System.exit(1);
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
   * Print something
   * (to be adapted in subclasses)
   */
  protected void doPrint(String msg) {
    System.out.println(msg);
  }
  
  /**
   * Print something on error channel
   * (to be adapted in subclasses)
   */
  protected void doErrPrint(String msg) {
    System.err.println(msg);
  }
  
  /**
   * Print stacktrace of a throwable
   * (to be adapted in subclasses)
   */
  protected void doPrintStackTrace(Throwable t) {
    t.printStackTrace(System.out);
  }

  /**
   * Print stacktrace of a throwable to error out
   * (to be adapted in subclasses)
   */
  protected void doErrPrintStackTrace(Throwable t) {
    t.printStackTrace(System.err);
  }


}
