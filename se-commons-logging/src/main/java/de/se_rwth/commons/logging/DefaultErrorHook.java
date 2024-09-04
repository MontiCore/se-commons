/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

public class DefaultErrorHook implements IErrorHook {

  @Override
  public void terminate() {
    System.exit(-1);
  }
}
