/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import java.util.stream.Collectors;

public class DefaultErrorHook implements IErrorHook {

  @Override
  public void terminate() {
    String messages = Log.getFindings().stream()
      .filter(Finding::isError)
      .map(Finding::toString)
      .collect(Collectors.joining(System.lineSeparator()));
    throw new MCFatalError(messages);
  }
}
