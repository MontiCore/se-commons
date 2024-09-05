/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.common;

import de.se_rwth.commons.logging.IErrorHook;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.MCFatalError;

import java.util.stream.Collectors;

public class GradleErrorHook implements IErrorHook {

  @Override
  public void terminate() {
    String messages = Log.getFindings().stream()
        .filter(Finding::isError)
        .map(Finding::toString)
        .collect(Collectors.joining(System.lineSeparator()));

    throw new MCFatalError(messages);
  }
}
