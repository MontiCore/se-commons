/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * This is a very basic test of the new centralized logging mechanism. The main
 * purpose is to demonstrate the API (it is rather pointless to test either
 * logback or the used test configuration).
 *
 */
public class LogTest {
  
  @BeforeClass
  public static void initLogging() {
    // disable fail quick for unit testing
    Log.enableFailQuick(false);
  }
  
  @Test
  public void demonstrateLogging() {
    // using the centralized logging to explicitly log messages for the USER(!);
    // these messages are logged to the console with level >= INFO (see
    // configuration)
    Log.trace("The application has started.", "the.start.component");
    Log.debug("An internal result is 'true'.", "an.internal.component");
    Throwable t = new RuntimeException("Oops!");
    Log.debug("Now we have an issue.", t, "an.internal.component");
    Log.info("Something went wrong.", "an.internal.component");
    Log.warn("Something went wrong.");
    Log.error("An internal error occured", t);
    
    // this is bogus but hey, it's a test
    assertNotNull("");
    
    // switch and demonstrate user logging
    
    demonstrateLogbackConfigurationForUser();
    
    // switch and demonstrate developer logging
    
    demonstrateLogbackConfigurationForDeveloper();
  }
  
  public void demonstrateLogbackConfigurationForUser() {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    
    try {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(context);
      
      context.reset();
      configurator.doConfigure(getClass().getClassLoader().getResourceAsStream("user.logging.xml"));
    }
    catch (JoranException e) {
      e.printStackTrace();
      fail();
    }
    
    Log.trace("The application has started.", "the.start.component");
    Log.debug("An internal result is 'true'.", "an.internal.component");
    Throwable t = new RuntimeException("Oops!");
    Log.debug("Now we have an issue.", t, "an.internal.component");
    Log.info("Something went wrong.", "an.internal.component");
    Log.warn("Something went wrong.");
    Log.error("An internal error occured", t);
  }
  
  public void demonstrateLogbackConfigurationForDeveloper() {
    // use slf4j logging
    Slf4jLog.init();
    
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    
    try {
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(context);
      
      context.reset();
      configurator.doConfigure(getClass().getClassLoader().getResourceAsStream(
          "developer.logging.xml"));
    }
    catch (JoranException e) {
      e.printStackTrace();
      fail();
    }
    
    Log.trace("The application has started.", "the.start.component");
    Log.debug("An internal result is 'true'.", "an.internal.component");
    Throwable t = new RuntimeException("Oops!");
    Log.debug("Now we have an issue.", t, "an.internal.component");
    Log.info("Something went wrong.", "an.internal.component");
    Log.warn("Something went wrong.");
  }
  
}
