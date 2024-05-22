/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class FileLogHook implements ILogHook {

  protected final String fileName;

  public FileLogHook(String fileName) {
    this.fileName = fileName;
    try {
      File dir = new File(fileName).getParentFile();
      if (dir.exists() || dir.mkdirs()) {
        new FileWriter(fileName, false).close();
      } else {
        System.err.print("Could not create directory '%s'.");
      }
    } catch (IOException e) {
      System.err.println("Initialization the file logger `" + fileName + "` threw ");
      e.printStackTrace(System.err);
      throw new MCFatalError(e.getMessage());
    }
  }

  @Override
  public void doPrintln(String msg) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
      writer.write(msg);
      writer.newLine();
      writer.close();
    } catch (IOException e) {
      System.err.printf("Writing message `%s` to the file logger `%s` threw ", msg, fileName);
      e.printStackTrace(System.err);
      throw new MCFatalError(e.getMessage());
    }
  }

  @Override
  public void doErrPrint(String msg) {
    doPrintln(msg);
  }

  @Override
  public void doPrint(String msg) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
      writer.write(msg);
      writer.close();
    } catch (IOException e) {
      System.err.printf("Writing message `%s` to the file logger `%s` threw ", msg, fileName);
      e.printStackTrace(System.err);
      throw new MCFatalError(e.getMessage());
    }
  }

  @Override
  public void doPrintStackTrace(Throwable t) {
    try {
      StringBuilder output = new StringBuilder();
      Arrays.stream(t.getStackTrace())
        .map(StackTraceElement::toString)
        .forEach(s -> output.append(s).append("\n"));

      BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
      writer.write(output.toString());
      writer.newLine();
      writer.close();
    } catch (IOException e) {
      System.err.println("Tried to log throwable ");
      t.printStackTrace(System.err);
      System.err.printf("But writing to the file logger `%s` threw %n", fileName);
      e.printStackTrace(System.err);
      throw new MCFatalError(e.getMessage());
    }
  }

  @Override
  public void doErrPrintStackTrace(Throwable t) {
    doPrintStackTrace(t);
  }

}
