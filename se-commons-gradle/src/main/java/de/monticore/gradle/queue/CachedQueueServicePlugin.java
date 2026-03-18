/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.queue;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

/**
 * Base plugin managing the cached isolation service
 */
public class CachedQueueServicePlugin implements Plugin<Project> {
  @Override
  public void apply(@NonNull Project project) {
    // Register the service in the settings plugin
    project.getGradle().getPluginManager().apply(InternalCachedQueueSetupSettingsPlugin.class);
    if (project == project.getRootProject()) {
      // Register an optional reporting task
      TaskProvider<ReportCachedQueueServiceTask> reportTask = project.getTasks().register("reportCachedQueueService", ReportCachedQueueServiceTask.class, spec -> {
        spec.mustRunAfter(project.getTasks().withType(ICachedQueueTask.class));
      });
      // TODO: https://git.rwth-aachen.de/monticore/monticore/-/issues/4949
//      project.getTasks().withType(ICachedQueueTask.class).configureEach(task -> {
//        task.finalizedBy(reportTask);
//      });
    }
  }

}
