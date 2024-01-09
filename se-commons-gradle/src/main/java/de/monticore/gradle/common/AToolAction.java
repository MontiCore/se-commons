/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.common;

import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.workers.WorkAction;

import java.util.Arrays;

/**
 * Action to represent the invocation of a tool.
 * Implement the {@link #doRun(String[])} method in your implementation.
 * The {@link ToolArgActionParameter} parameters contain
 *  - the String[] tool-args
 *  - (opt) a DisplayName, which will be reported
 *  - (opt) a {@link de.monticore.gradle.internal.ProgressLoggerService},
 *    which will be used to report the progress of the individual workactions
 */
public abstract class AToolAction implements WorkAction<ToolArgActionParameter> {

  @Override
  public void execute() {
    String[] args = this.getParameters().getArgs().get().toArray(new String[0]);
    String description = Arrays.toString(args);

    if (getParameters().getProgressLogger().isPresent()) {
      // Only report on the progress, if the progress logger is set
      ProgressLoggerFactory progressLoggerFactory = getParameters().getProgressLogger().get().getFactory();
      ProgressLogger pl = progressLoggerFactory.newOperation("mc");
      // report the start of our action
      pl.start(description, this.getParameters().getProgressName().getOrElse(description));
      // do run the action
      doRun(args);
      // report the competition of our action
      pl.completed();
    } else {
      // just run the action without a progress logger
      doRun(args);
    }
  }

  protected abstract void doRun(String[] args);
}
