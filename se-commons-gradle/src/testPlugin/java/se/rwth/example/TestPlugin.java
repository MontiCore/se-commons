/* (c) https://github.com/MontiCore/monticore */

package se.rwth.example;

import de.monticore.gradle.internal.ProgressLoggerService;
import de.monticore.gradle.queue.CachedQueueServicePlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

public class TestPlugin implements Plugin<Project> {

  @Override
  public void apply(@NonNull Project project) {
    
    // ServiceProvider to pass a ProgressLogger instance to the workers
    Provider<ProgressLoggerService> serviceProvider = project.getGradle().getSharedServices()
        .registerIfAbsent(ProgressLoggerService.class.getSimpleName(),
            ProgressLoggerService.class, spec -> {});
    
    // Register the service
    project.getPluginManager().apply(CachedQueueServicePlugin.class);

    TaskProvider<ReportStatsTask> reportStatsTask = project.getTasks().register("reportStats", ReportStatsTask.class, task -> {
    });

    project.getTasks().withType(ExampleTask.class).configureEach(task -> {
      task.finalizedBy(reportStatsTask);
      task.getProgressLoggerService().set(serviceProvider);
    });
  }
}
