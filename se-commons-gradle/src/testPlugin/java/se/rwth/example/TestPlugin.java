/* (c) https://github.com/MontiCore/monticore */

package se.rwth.example;

import de.monticore.gradle.internal.ProgressLoggerService;
import de.monticore.gradle.queue.CachedQueueServicePlugin;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

public class TestPlugin implements Plugin<Project> {

  @Override
  public void apply(@NonNull Project project) {
    // Report the plugin classloader (used by tests to check for multiple plugin classloaders)
    System.out.println("TestPlugin-CL " + System.identityHashCode(TestPlugin.class.getClassLoader()));
    
    // ServiceProvider to pass a ProgressLogger instance to the workers
    Provider<ProgressLoggerService> serviceProvider = project.getGradle().getSharedServices()
        // A name per classloader, as the plugin might be loaded by multiple classloaders
        .registerIfAbsent(ProgressLoggerService.class.getSimpleName() + "@"
                + System.identityHashCode(ProgressLoggerService.class.getClassLoader()),
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
