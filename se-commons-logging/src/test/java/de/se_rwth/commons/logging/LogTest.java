/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is a very basic test of the new centralized logging mechanism. The main
 * purpose is to demonstrate the API (it is rather pointless to test either
 * logback or the used test configuration).
 *
 */
@Execution(ExecutionMode.SAME_THREAD)
public class LogTest {

  @Test
  public void testLogHooksSetters() {
    Log.init();
    // Test default setup
    Assertions.assertEquals(1, Log.getLog().logHooks.size());
    Assertions.assertInstanceOf(ConsoleLogHook.class, Log.getLog().logHooks.get(0));

    // Test adding a new loghook
    ILogHook logHook = new FileLogHook("target/test/placeholder.txt");
    Log.addLogHook(logHook);
    Assertions.assertEquals(2, Log.getLog().logHooks.size());
    Assertions.assertInstanceOf(ConsoleLogHook.class, Log.getLog().logHooks.get(0));
    Assertions.assertEquals(logHook, Log.getLog().logHooks.get(1));

    // Test removing
    Assertions.assertTrue(Log.removeLogHook(logHook));
    Assertions.assertEquals(1, Log.getLog().logHooks.size());
    Assertions.assertInstanceOf(ConsoleLogHook.class, Log.getLog().logHooks.get(0));

    // Test removing twice -> nothing to remove
    Assertions.assertFalse(Log.removeLogHook(logHook));

    // Test remove via class
    Assertions.assertTrue(Log.removeLogHook(ConsoleLogHook.class));
    Assertions.assertEquals(0, Log.getLog().logHooks.size());

    // Test removing twice -> nothing to remove
    Assertions.assertFalse(Log.removeLogHook(ConsoleLogHook.class));
  }

  @Test
  public void demonstrateLogging() {
    LogStub.init();
    Log.enableFailQuick(false);

    // using the centralized logging to explicitly log messages for the USER(!);
    // these messages are logged to the console with level >= INFO (see
    // configuration)
    Log.trace("The application has started.", "the.start.component");
    Log.debug("An internal result is 'true'.", "an.internal.component");
    Throwable t = new RuntimeException("Oops!");
    Log.debug("Now we have an issue.", t, "an.internal.component");
    Log.info("Something went wrong.", "an.internal.component");
    Log.warn("Something went wrong.");
    Log.error("An internal error occurred", t);

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
    } catch (JoranException e) {
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
    } catch (JoranException e) {
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

  @Test
  public void testAndDemonstrateLogStubForDeveloper() {
    // use stub (that stores the prints and the errors/warnings)
    LogStub.init();

    Log.print("line 1");
    Log.println("line 2");
    Log.print("line 3\n");
    List<String> r1 = LogStub.getPrints();
    assertEquals(3, r1.size());
    assertEquals("line 1", r1.get(0));
    assertEquals("line 2" + System.lineSeparator(), r1.get(1));
    assertEquals("line 3\n", r1.get(2));

    LogStub.clearPrints();
    Log.print("line 4");
    Log.println("line 5");
    List<String> r2 = LogStub.getPrints();
    assertEquals(2, r2.size());
    assertEquals("line 4", r2.get(0));
  }

  @Test
  public void testFileOutput() {
    LogStub.init();
    String fileName = "target/test/LogOutput.txt";
    Log.addLogHook(new FileLogHook(fileName));

    Log.println("line 1");
    Log.println("line 2");
    Log.println("line 3");

    assertTrue(new File(fileName).exists());

    Log.warn("Warning", new RuntimeException("this is an exception"));
  }

  @Test
  public void testErrorWithoutQF() {
    // Test without quick fail -> exactly two errors are printed to stdout, nothing else
    Log.init();
    LinePosition e1 = new LinePosition(), e2 = new LinePosition(), e3 = new LinePosition();
    this.withLogSetup(() -> {
      Log.enableFailQuick(false);
      Log.error("First error in line " + e1.trackCurrentLine(), new RuntimeException("E1"));
      Log.error("Second error in line " + e2.trackCurrentLine(), new RuntimeException("E2"));
      Log.error("Third error in line " + e3.trackCurrentLine());
    }, setup -> {
      Assertions.assertFalse(setup.exitCalled);
      // Then check actual output
      Assertions.assertEquals(
              "[ERROR]  First error in line " + e1.getLine() + System.lineSeparator() +
                      "[ERROR]  Second error in line " + e2.getLine() + System.lineSeparator() +
                      "[ERROR]  Third error in line " + e3.getLine() + System.lineSeparator()
              , setup.real_std.toString());
      Assertions.assertEquals("", setup.real_ste.toString());
    });
  }


  @Test
  public void testErrorStacktraceWithoutQF() {
    // Test without quick fail and with exceptions =>
    Log.init();
    LinePosition e1 = new LinePosition(), e2 = new LinePosition(), e3 = new LinePosition();
    this.withLogSetup(() -> {
      Log.enableFailQuick(false);
      Log.addLogHook(new ErrorStacktraceConsoleLogHook(System.out));
      Log.error("First error in line " + e1.trackCurrentLine(), new RuntimeException("E1"));
      Log.error("Second error in line " + e2.trackCurrentLine(), new RuntimeException("E2"));
      Log.error("Third error in line " + e3.trackCurrentLine());
    }, setup -> {
      Assertions.assertFalse(setup.exitCalled);
      // Then check actual output
      String[] real_std = setup.real_std.toString().split(System.lineSeparator());
      Assertions.assertEquals("[ERROR]  First error in line " + e1.getLine(), real_std[0]);
      Assertions.assertTrue(real_std[1].startsWith("\tat de.se_rwth.commons.logging.Log.error(Log.java:"), real_std[1]);
      // We now skip over the stacktrace until we find the exception
      int lineCounter = 1;
      while (!real_std[lineCounter].startsWith("Caused by ")) {
        assertFalse(real_std[lineCounter].startsWith("[ERROR]"));
        lineCounter++;
      }
      Assertions.assertEquals("Caused by java.lang.RuntimeException: E1", real_std[lineCounter]);
      lineCounter++;
      while (!real_std[lineCounter].startsWith("[ERROR]  Second error in line " + e2.getLine())) {
        assertFalse(real_std[lineCounter].startsWith("Caused by"));
        lineCounter++;
      }
      lineCounter++;
      while (!real_std[lineCounter].startsWith("Caused by ")) {
        assertFalse(real_std[lineCounter].startsWith("[ERROR]"));
        lineCounter++;
      }
      Assertions.assertEquals("Caused by java.lang.RuntimeException: E2", real_std[lineCounter]);

      Assertions.assertEquals("", setup.real_ste.toString());
    });
  }

  @Test
  public void testErrorQF() {
    Log.init();
    Log.enableFailQuick(true);

    LinePosition e1 = new LinePosition(), e2 = new LinePosition();

    this.withLogSetup(() -> {
      Log.error("First error in line " + e1.trackCurrentLine());
      Log.error("Second error in line " + e2.trackCurrentLine());
    }, setup -> {
      Assertions.assertTrue(setup.exitCalled);
      Assertions.assertEquals(
              "[ERROR]  First error in line " + e1.getLine() + System.lineSeparator()
              , setup.real_std.toString());
      Assertions.assertEquals("", setup.real_ste.toString());
    });
  }


  LogTestSetup withLogSetup(Runnable runnable, Consumer<LogTestSetup> consumer) {
    LogTestSetup setup = new LogTestSetup();
    Log.setErrorHook(() -> {
      setup.exitCalled = true;
      throw new SystemExitMocking();
    });

    var originalOut = System.out;
    var originalErr = System.err;
    try {
      System.setOut(new PrintStream(tee(originalOut, setup.real_std), true));
      System.setErr(new PrintStream(tee(originalErr, setup.real_ste), true));
      runnable.run();
    } catch (SystemExitMocking e) {
      // do not pass upwards
    } finally {
      System.setOut(originalOut);
      System.setErr(originalErr);
    }

    consumer.accept(setup);

    return setup;
  }

  OutputStream tee(OutputStream s1, OutputStream s2) {
    return new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        s1.write(b);
        s2.write(b);
      }

      @Override
      public void write(byte[] b) throws IOException {
        s1.write(b);
        s2.write(b);
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        s1.write(b, off, len);
        s2.write(b, off, len);
      }

      @Override
      public void flush() throws IOException {
        s1.flush();
        s2.flush();
      }

      @Override
      public void close() throws IOException {
        s1.close();
        s2.close();
      }
    };
  }

  static class LinePosition {
    int currentLine = -42;

    public int trackCurrentLine() {
      if (currentLine != -42)
        throw new IllegalStateException("Line already tracked in line " + currentLine);
      currentLine = new Throwable().getStackTrace()[1].getLineNumber();
      return currentLine;
    }

    public int getLine() {
      if (currentLine == -42)
        throw new IllegalStateException("Line was not tracked");
      return currentLine;
    }
  }

  static class LogTestSetup {

    ByteArrayOutputStream real_std = new ByteArrayOutputStream();
    ByteArrayOutputStream real_ste = new ByteArrayOutputStream();

    boolean exitCalled = false;
  }

  static class SystemExitMocking extends RuntimeException {

  }

}
