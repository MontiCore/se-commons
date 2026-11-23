/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.queue;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;

/**
 * Base plugin managing the cached isolation service.
 * <p>
 * Note: This plugin might be applied multiple times per build, once per classloader the
 * se-commons-gradle library is loaded with (e.g., when unrelated plugins are applied to different
 * subprojects). Only the first application registers the {@link CachedQueueService}; all further
 * ones share this instance (via {@link ICachedQueueService#asICachedQueueService(Object)}).
 */
public class InternalCachedQueueSetupSettingsPlugin implements Plugin<Gradle> {
  protected Logger logger = Logging.getLogger(CachedQueueService.class);

  @Override
  public void apply(@NonNull Gradle gradle) {
    // Register the service, if needed
    Provider<CachedQueueService> sp = gradle.getSharedServices().registerIfAbsent(CachedQueueService.NAME, CachedQueueService.class, spec -> {
    });
    // Do not cast the service: It might have been registered from a different classloader
    Object service = ((Provider<?>) sp).get();
    if (service instanceof CachedQueueService) {
      ((CachedQueueService) service).init(gradle);
    } else {
      // The service was registered (and initialized) by the copy of this plugin within another classloader.
      // It is shared with the tasks of this classloader using a proxy.
      logger.info("The se-commons cached work queue was registered by a different classloader ({}). "
          + "Sharing it with the tasks of classloader {}", service.getClass().getClassLoader(),
          CachedQueueService.class.getClassLoader());
    }
    gradle.allprojects(p -> withType(p, sp));
  }

  protected void withType(Project p, Provider<CachedQueueService> sp) {
    p.getTasks().withType(ICachedQueueTask.class).configureEach(task -> {
      task.getSharedQueueServiceProperty().set(sp);
      task.usesService(sp);
    });
    // The report task has to avoid circular dependencies
    p.getTasks().withType(ReportCachedQueueServiceTask.class).configureEach(task -> {
      task.getSharedQueueService().set(sp);
      task.usesService(sp);
    });
  }

}
