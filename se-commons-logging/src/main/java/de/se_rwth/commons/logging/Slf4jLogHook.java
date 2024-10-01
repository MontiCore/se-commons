/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/**
 * The slf4j variant of {@link Log}. Use {@link Slf4jLog#replaceLogHooksWithSlf4j()} to enable
 * usage.
 */
public class Slf4jLogHook implements ILogHook {

  // the slf4j logger factory (bound at runtime)
  ILoggerFactory factory = LoggerFactory.getILoggerFactory();

  protected boolean doIsSlf4jTraceEnabled(String logName) {
    return factory.getLogger(logName).isTraceEnabled();
  }

  protected boolean doIsSlf4jDebugEnabled(String logName) {
    return factory.getLogger(logName).isDebugEnabled();
  }

  protected boolean doIsSlf4jInfoEnabled(String logName) {
    return factory.getLogger(logName).isInfoEnabled();
  }

  @Override
  public void doTrace(String msg, String logName) {
    if (this.doIsSlf4jTraceEnabled(logName)) {
      factory.getLogger(logName).trace(msg);
    }
  }

  @Override
  public void doTrace(String msg, Throwable t, String logName) {
    if (this.doIsSlf4jTraceEnabled(logName)) {
      factory.getLogger(logName).trace(msg, t);
    }
  }


  @Override
  public void doDebug(String msg, String logName) {
    if (this.doIsSlf4jDebugEnabled(logName)) {
      factory.getLogger(logName).debug(msg);
    }
  }

  @Override
  public void doDebug(String msg, Throwable t, String logName) {
    if (this.doIsSlf4jDebugEnabled(logName)) {
      factory.getLogger(logName).debug(msg, t);
    }
  }

  @Override
  public void doInfo(String msg, String logName) {
    if (this.doIsSlf4jInfoEnabled(logName)) {
      factory.getLogger(logName).info(msg);
    }
  }

  @Override
  public void doInfo(String msg, Throwable t, String logName) {
    if (this.doIsSlf4jInfoEnabled(logName)) {
      factory.getLogger(logName).info(msg, t);
    }
  }

  @Override
  public void doWarn(Finding warn) {
    factory.getLogger(ROOT_LOGGER_NAME).warn(warn.toString());
  }

  @Override
  public void doWarn(Finding warn, Throwable t) {
    factory.getLogger(ROOT_LOGGER_NAME).warn(warn.toString(), t);
  }

  @Override
  public void doError(Finding error) {
    factory.getLogger(ROOT_LOGGER_NAME).error(error.toString());
  }

  @Override
  public void doError(Finding error, Throwable t) {
    factory.getLogger(ROOT_LOGGER_NAME).error(error.toString(), t);
  }

  @Override
  public void doPrintln(String msg) {
    // empty
  }

  @Override
  public void doErrPrint(String msg) {
    // empty
  }

  @Override
  public void doPrintStackTrace(Throwable t) {
    // empty
  }

  @Override
  public void doErrPrintStackTrace(Throwable t) {
    // empty
  }

  @Override
  public void doPrint(String msg) {
    // empty
  }
}
