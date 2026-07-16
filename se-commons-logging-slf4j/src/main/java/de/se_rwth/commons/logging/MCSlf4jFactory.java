/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory for MCLoggerAdapter.
 */
public class MCSlf4jFactory implements ILoggerFactory {

  private final ConcurrentMap<String, Logger> loggerMap = new ConcurrentHashMap<>();

  @Override
  public Logger getLogger(String name) {
    return loggerMap.computeIfAbsent(name, MCSlf4jAdapter::new);
  }
}
