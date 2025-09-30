/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.queue;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

/**
 * Base plugin managing the cached isolation service
 */
public class CachedQueueServicePlugin implements Plugin<Project> {
  @Override
  public void apply(@NonNull Project project) {
    // Register the service, if needed
    Provider<CachedQueueService> sp = project.getGradle().getSharedServices().registerIfAbsent(CachedQueueService.NAME, CachedQueueService.class, spec -> {
    });
    if (project == project.getRootProject()) {
      // initialize the service but only if it is the root project
      sp.get().init(project.getGradle());

      // Register an optional reporting task
      TaskProvider<ReportCachedQueueServiceTask> reportTask = project.getTasks().register("reportCachedQueueService", ReportCachedQueueServiceTask.class, spec -> {
        spec.mustRunAfter(project.getTasks().withType(ICachedQueueTask.class));
        spec.getSharedQueueService().set(sp);
      });
      project.getTasks().withType(ICachedQueueTask.class).configureEach(task -> {
        task.finalizedBy(reportTask);
      });
    }


    // And configure all tasks with the ICachedQueueTask interface to access this service

    project.getTasks().withType(ICachedQueueTask.class).configureEach(task -> {
      task.getSharedQueueService().set(sp);
    });
  }

}
