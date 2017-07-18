/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
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

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import de.se_rwth.commons.SourcePosition;

/**
 * The slf4j variant of {@link Log}. Use {@link Slf4jLog#init()} to enable
 * usage.
 *
 * @author (last commit) $Author$
 * @version $Revision$, $Date: 2015-09-04 14:18:19 +0200 (Fr, 04 Sep 2015) $
 */
public class Slf4jLog extends Log {
  
  /**
   * Initializes slf4j backed logging as the centralized logging component.
   */
  public static final void init() {
    setLog(new Slf4jLog());
  }
  
  // the slf4j logger factory (bound at runtime)
  ILoggerFactory factory = LoggerFactory.getILoggerFactory();
  
  /* Utility class. */
  protected Slf4jLog() {
  }
  
  @Override
  protected boolean doIsTraceEnabled(String logName) {
    return factory.getLogger(logName).isTraceEnabled();
  }
  
  @Override
  protected void doTrace(String msg, String logName) {
    factory.getLogger(logName).trace(msg);
  }
  
  @Override
  protected void doTrace(String msg, Throwable t, String logName) {
    factory.getLogger(logName).trace(msg, t);
  }
  
  @Override
  protected boolean doIsDebugEnabled(String logName) {
    return factory.getLogger(logName).isDebugEnabled();
  }
  
  @Override
  protected void doDebug(String msg, String logName) {
    factory.getLogger(logName).debug(msg);
  }
  
  @Override
  protected void doDebug(String msg, Throwable t, String logName) {
    factory.getLogger(logName).debug(msg, t);
  }
  
  @Override
  protected boolean doIsInfoEnabled(String logName) {
    return factory.getLogger(logName).isInfoEnabled();
  }
  
  @Override
  protected void doInfo(String msg, String logName) {
    factory.getLogger(logName).info(msg);
  }
  
  @Override
  protected void doInfo(String msg, Throwable t, String logName) {
    factory.getLogger(logName).info(msg, t);
  }
  
  @Override
  protected void doWarn(String msg) {
    Finding warn = Finding.warning(msg);
    factory.getLogger(ROOT_LOGGER_NAME).warn(warn.toString());
    addFinding(warn);
  }
  
  @Override
  protected void doWarn(String msg, SourcePosition pos) {
    Finding warn = Finding.warning(msg, pos);
    factory.getLogger(ROOT_LOGGER_NAME).warn(warn.toString());
    addFinding(warn);
  }
  
  @Override
  protected void doWarn(String msg, Throwable t) {
    Finding warn = Finding.warning(msg);
    factory.getLogger(ROOT_LOGGER_NAME).warn(warn.toString(), t);
    addFinding(warn);
  }
  
  @Override
  protected void doError(String msg) {
    Finding error = Finding.error(msg);
    factory.getLogger(ROOT_LOGGER_NAME).error(error.toString());
    addFinding(error);
    terminateIfErrors();
  }
  
  @Override
  protected void doError(String msg, SourcePosition pos) {
    Finding error = Finding.error(msg, pos);
    factory.getLogger(ROOT_LOGGER_NAME).error(error.toString());
    addFinding(error);
    terminateIfErrors();
  }
  
  @Override
  protected void doError(String msg, Throwable t) {
    Finding error = Finding.error(msg);
    factory.getLogger(ROOT_LOGGER_NAME).error(error.toString(), t);
    addFinding(error);
    terminateIfErrors();
  }
  
}
