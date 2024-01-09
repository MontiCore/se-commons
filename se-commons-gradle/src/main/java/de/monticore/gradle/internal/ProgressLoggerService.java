/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal;

import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;

import javax.annotation.Nonnull;

/**
 * We use a build service to pass a ProgressLogger instance to the workers
 * See https://github.com/gradle/gradle/issues/2678
 */
public abstract class ProgressLoggerService implements BuildService<BuildServiceParameters.None> {

  protected ProgressLoggerFactory factory;

  public ProgressLoggerFactory getFactory() {
    return factory;
  }

  public void setFactory(@Nonnull ProgressLoggerFactory factory) {
    this.factory = factory;
  }
}
