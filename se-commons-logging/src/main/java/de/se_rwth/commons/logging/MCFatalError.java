package de.se_rwth.commons.logging;

/**
 * A fatal error, which should result in an abort
 */
public class MCFatalError extends Error {
  public MCFatalError(String message) {
    super(message);
  }
}
