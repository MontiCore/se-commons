/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;

/**
 * Adapter for SLF4J Logger to MontiCore Log.
 */
public class MCSlf4jAdapter extends LegacyAbstractLogger {

  protected static final ThreadLocal<Boolean> RECURSION_GUARD = ThreadLocal.withInitial(() -> false);

  public MCSlf4jAdapter(String name) {
    this.name = name;
  }

  @Override
  public boolean isTraceEnabled() {
    return Log.isTraceEnabled(name);
  }

  @Override
  public boolean isDebugEnabled() {
    return Log.isDebugEnabled(name);
  }

  @Override
  public boolean isInfoEnabled() {
    return Log.isInfoEnabled(name);
  }

  @Override
  public boolean isWarnEnabled() {
    return true;
  }

  @Override
  public boolean isErrorEnabled() {
    return true;
  }

  @Override
  protected String getFullyQualifiedCallerName() {
    return null; // Not needed
  }

  @Override
  protected void handleNormalizedLoggingCall(Level level, Marker marker, String msg, Object[] arguments, Throwable t) {
    if (RECURSION_GUARD.get()) {
      throw new IllegalStateException("Circular logging configuration detected");
    }
    RECURSION_GUARD.set(true);


    String formattedMessage = msg;
    Throwable finalThrowable = t;

    if (arguments != null && arguments.length > 0) {
      FormattingTuple ft = MessageFormatter.arrayFormat(msg, arguments);
      formattedMessage = ft.getMessage();
      if (finalThrowable == null) {
        finalThrowable = ft.getThrowable();
      }
    }

    switch (level) {
      case TRACE:
        if (finalThrowable == null) {
          Log.trace(formattedMessage, name);
        }
        else {
          Log.trace(formattedMessage, finalThrowable, name);
        }
        break;
      case DEBUG:
        if (finalThrowable == null) {
          Log.debug(formattedMessage, name);
        }
        else {
          Log.debug(formattedMessage, finalThrowable, name);
        }
        break;
      case INFO:
        if (finalThrowable == null) {
          Log.info(formattedMessage, name);
        }
        else {
          Log.info(formattedMessage, finalThrowable, name);
        }
        break;
      case WARN:
        if (finalThrowable == null) {
          Log.warn(formattedMessage + " [" + name + "]");
        }
        else {
          Log.warn(formattedMessage + " [" + name + "]", finalThrowable);
        }
        break;
      case ERROR:
        if (finalThrowable == null) {
          Log.error(formattedMessage + " [" + name + "]");
        }
        else {
          Log.error(formattedMessage + " [" + name + "]", finalThrowable);
        }
        break;
    }

    RECURSION_GUARD.set(false);
  }
}
