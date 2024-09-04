/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.common;

import de.se_rwth.commons.logging.IErrorHook;
import org.gradle.api.GradleException;

public class GradleErrorHook implements IErrorHook {

  @Override
  public void terminate() {
    throw new GradleException("There were compile errors.");
  }
}
