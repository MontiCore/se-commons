/* (c) https://github.com/MontiCore/monticore */
package se.rwth.example;

import de.monticore.gradle.queue.ICachedQueueTask;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * Example task that reports its state to std::err
 */
public abstract class ReportStatsTask extends DefaultTask implements ICachedQueueTask {

  @TaskAction
  public void execute() {
    System.err.println("Stats[[" + doGetSharedQueueService().getStats() + "]]Stats");
  }

}
