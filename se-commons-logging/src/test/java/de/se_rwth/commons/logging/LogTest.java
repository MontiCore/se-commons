/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is a very basic test of the new centralized logging mechanism. The main
 * purpose is to demonstrate the API (it is rather pointless to test either
 * logback or the used test configuration).
 *
 */
public class LogTest {
  
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
    Log.init();
    Log.enableFailQuick(false);
    LinePosition e1 = new LinePosition(), e2 = new LinePosition();

    this.withLogSetup(() -> {
      Log.error("First error in line " + e1.getCurrentLine(), new RuntimeException("E1"));
      Log.error("Second error in line " + e2.getCurrentLine(), new RuntimeException("E2"));
    }, setup -> {
      Assertions.assertFalse(setup.exitCalled);
      // First: Check calls
      Assertions.assertArrayEquals(new String[]{
                      "<println>[ERROR]  First error in line " + e1.getCurrentLine(),
                      "<println>[ERROR]  Second error in line " + e2.getCurrentLine()},
              setup.std.toString().split("\n"));
      // Then check actual output
      Assertions.assertEquals(
              "[ERROR]  First error in line " + e1.getCurrentLine() + System.lineSeparator() +
                      "[ERROR]  Second error in line " + e2.getCurrentLine() + System.lineSeparator()
              , setup.real_std.toString());
      Assertions.assertEquals("", setup.ste.toString());
    });
  }


  @Test
  public void testErrorStacktraceWithoutQF() {
    Log.init();
    Log.enableFailQuick(false);
    Log.addLogHook(new ErrorStacktraceConsoleLogHook(System.out));


    LinePosition e1 = new LinePosition(), e2 = new LinePosition();

    this.withLogSetup(() -> {
      Log.error("First error in line " + e1.getCurrentLine(), new RuntimeException("E1"));
      Log.error("Second error in line " + e2.getCurrentLine(), new RuntimeException("E2"));
    }, setup -> {
      Assertions.assertFalse(setup.exitCalled);
      // First: Check calls
      Assertions.assertArrayEquals(new String[]{
                      "<println>[ERROR]  First error in line " + e1.getCurrentLine(),
                      "<println>[ERROR]  Second error in line " + e2.getCurrentLine()},
              setup.std.toString().split("\n"));
      // Then check actual output
      String[] real_std = setup.real_std.toString().split(System.lineSeparator());
      Assertions.assertEquals("[ERROR]  First error in line " + e1.getCurrentLine(), real_std[0]);
      Assertions.assertTrue(real_std[1].startsWith("\tat de.se_rwth.commons.logging.Log.error(Log.java:"), real_std[1]);
      int lineCounter = 1;
      while (!real_std[lineCounter].startsWith("Caused by ")) {
        assertFalse(real_std[lineCounter].startsWith("[ERROR]"));
        lineCounter++;
      }
      Assertions.assertEquals("Caused by java.lang.RuntimeException: E1", real_std[lineCounter]);
      lineCounter++;
      while (!real_std[lineCounter].startsWith("[ERROR]  Second error in line " + e2.getCurrentLine())) {
        assertFalse(real_std[lineCounter].startsWith("Caused by"));
        lineCounter++;
      }
      lineCounter++;
      while (!real_std[lineCounter].startsWith("Caused by ")) {
        assertFalse(real_std[lineCounter].startsWith("[ERROR]"));
        lineCounter++;
      }
      Assertions.assertEquals("Caused by java.lang.RuntimeException: E2", real_std[lineCounter]);

      Assertions.assertEquals("", setup.ste.toString());
    });
  }

  @Test
  public void testErrorQF() {
    Log.init();
    Log.enableFailQuick(true);

    LinePosition e1 = new LinePosition(), e2 = new LinePosition();

    this.withLogSetup(() -> {
      Log.error("First error in line " + e1.getCurrentLine());
      Log.error("Second error in line " + e2.getCurrentLine());
    }, setup -> {
      Assertions.assertTrue(setup.exitCalled);
      String[] info = setup.std.toString().split("\n");
      Assertions.assertEquals(1,info.length, Arrays.toString(info));
      Assertions.assertEquals("<println>[ERROR]  First error in line " + e1.getCurrentLine(), info[0]);
      Assertions.assertEquals("", setup.ste.toString());
    });
  }



  LogTestSetup withLogSetup(Runnable runnable, Consumer<LogTestSetup> consumer) {
    LogTestSetup setup = new LogTestSetup();
    Log.setErrorHook(() -> {
      setup.exitCalled = true;
      throw new  SystemExitMocking();
    });
    Log.addLogHook(new ErrorCollector(new PrintStream(setup.std),
            new PrintStream(setup.ste)));

    var originalOut = System.out;
    var originalErr = System.err;
    try {
      System.setOut(new PrintStream(tee(originalOut, setup.real_std), true));
      System.setErr(new PrintStream(tee(originalErr, setup.real_ste), true));
      runnable.run();
    }catch (SystemExitMocking e) {
      // do not pass upwards
    }
    finally {
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

  class LinePosition {
    int currentLine = -42;
    public int getCurrentLine() {
      if (currentLine == -42)
        currentLine = new Throwable().getStackTrace()[1].getLineNumber();
      return currentLine;
    }
  }

  class LogTestSetup   {
    ByteArrayOutputStream std = new ByteArrayOutputStream();
    ByteArrayOutputStream ste = new ByteArrayOutputStream();

    ByteArrayOutputStream real_std = new ByteArrayOutputStream();
    ByteArrayOutputStream real_ste = new ByteArrayOutputStream();

    boolean exitCalled = false;
  }

  class SystemExitMocking extends RuntimeException {

  }

  static class ErrorCollector implements ILogHook {
    PrintStream std,ste;

    public ErrorCollector(PrintStream  std, PrintStream  ste) {
      this.std = std;
      this.ste = ste;
    }

    @Override
    public void doPrintln(String msg) {
      std.append("<println>").append(msg).append("\n");
    }

    @Override
    public void doErrPrint(String msg) {
      ste.append("<print>").append(msg).append("\n");
    }

    @Override
    public void doPrintStackTrace(Throwable t) {
      std.append("<printstacktrace>");
      t.printStackTrace(std);
    }

    @Override
    public void doErrPrintStackTrace(Throwable t) {
      std.append("<printstacktrace>");
      t.printStackTrace(ste);
    }

    @Override
    public void doPrint(String msg) {
      std.append("<print>").append(msg);
    }
  }
}
