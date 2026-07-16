/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Holds tests for {@link MCSlf4jServiceProvider}
 */
public class MCSlf4jServiceProviderTest {

  @BeforeEach
  public void setUp() {
    LogStub.init();
    Log.enableFailQuick(false);
  }

  @Test
  public void testSLF4JToMCLogDelegation() {
    // Given
    Logger logger = LoggerFactory.getLogger("test.logger");
    assertInstanceOf(MCSlf4jAdapter.class, logger);

    // When
    logger.info("Hello SLF4J");
    logger.warn("Warning from SLF4J");

    // Then
    List<Finding> findings = Log.getFindings();
    assertEquals(1, findings.size());
    assertTrue(findings.get(0).getMsg().contains("Warning from SLF4J"));
    assertTrue(findings.get(0).getMsg().contains("[test.logger]"));
  }

  @Test
  public void testTraceDisabled() {
    // Given
    Logger logger = LoggerFactory.getLogger("test.trace");

    // Then
    assertFalse(logger.isTraceEnabled());
  }

  @Test
  public void testTraceEnabled() {
    // Given
    Log.initTRACE();
    Logger logger = LoggerFactory.getLogger("test.trace");

    // Then
    assertTrue(logger.isTraceEnabled());
  }

  /**
   * Infinite recursion of using Slf4 as the backend and frontend for our logger
   */
  @Test
  public void testRecursion() {
    // Given
    Slf4jLog.init();

    // When
    Logger logger = LoggerFactory.getLogger("test");

    // Then
    assertThrows(IllegalStateException.class, () -> {
      logger.info("This should fail with StackOverflowError");
    });
  }
}
