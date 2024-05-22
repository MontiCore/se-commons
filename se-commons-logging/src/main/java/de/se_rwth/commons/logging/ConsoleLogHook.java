/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

public class ConsoleLogHook implements ILogHook {

  @Override
  public void doPrintln(String msg) {
    System.out.println(msg);
  }

  @Override
  public void doErrPrint(String msg) {
    System.err.println(msg);
  }

  @Override
  public void doPrintStackTrace(Throwable t) {
    t.printStackTrace(System.out);
  }

  @Override
  public void doErrPrintStackTrace(Throwable t) {
    t.printStackTrace(System.err);
  }

  @Override
  public void doPrint(String msg) {
    System.out.print(msg);
  }

}
