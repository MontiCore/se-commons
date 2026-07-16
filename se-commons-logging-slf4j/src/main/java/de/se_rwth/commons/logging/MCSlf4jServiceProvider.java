/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * SLF4J Service Provider for MontiCore Log.
 * This allows libraries that use SLF4J to use our {@link Log} implementation
 */
public class MCSlf4jServiceProvider implements SLF4JServiceProvider {

  private ILoggerFactory loggerFactory;
  private IMarkerFactory markerFactory;
  private MDCAdapter mdcAdapter;

  @Override
  public ILoggerFactory getLoggerFactory() {
    return loggerFactory;
  }

  @Override
  public IMarkerFactory getMarkerFactory() {
    return markerFactory;
  }

  @Override
  public MDCAdapter getMDCAdapter() {
    return mdcAdapter;
  }

  @Override
  public String getRequestedApiVersion() {
    return "2.0.99";
  }

  @Override
  public void initialize() {
    loggerFactory = new MCSlf4jFactory();
    markerFactory = new BasicMarkerFactory();
    mdcAdapter = new NOPMDCAdapter();
  }
}
