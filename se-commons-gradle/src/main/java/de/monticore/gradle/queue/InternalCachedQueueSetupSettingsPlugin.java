/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.queue;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;

/**
 * Base plugin managing the cached isolation service
 */
public class InternalCachedQueueSetupSettingsPlugin implements Plugin<Gradle> {
  protected Logger logger = Logging.getLogger(CachedQueueService.class);
  @Override
  public void apply(@NonNull Gradle gradle) {
    // Register the service, if needed
    Provider<CachedQueueService> sp = gradle.getSharedServices().registerIfAbsent(CachedQueueService.NAME, CachedQueueService.class, spec -> {
    });
    try {
      sp.get().init(gradle);
    }catch (ClassCastException ignored) {
      logger.warn("WARNING: The se-commons cached work queue is setup multiple times.\n"
          + "Reporting, log prefixes, and performance might be incorrect. \n"
          + "Please add the (possibly MontiCore Generator) plugin to your root plugin (possible with apply false). \n"
          + "More details: https://monticore.github.io/monticore/docs/Gradle/#root-warning");
    }
    gradle.allprojects(p->withType(p, sp));
  }
  
  protected void withType(Project p, Provider<CachedQueueService> sp) {
    p.getTasks().withType(ICachedQueueTask.class).configureEach(task -> {
      task.getSharedQueueServiceProperty().set(sp);
    });
    // The report task has to avoid circular dependencies
    p.getTasks().withType(ReportCachedQueueServiceTask.class).configureEach(task -> {
      task.getSharedQueueService().set(sp);
    });
  }

}
