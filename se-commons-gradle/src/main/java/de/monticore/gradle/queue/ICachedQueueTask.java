/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.queue;

import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

/**
 * A task with access to a cached queue service.
 * Automatically managed via the {@link CachedQueueServicePlugin}
 */
public interface ICachedQueueTask extends Task {
  @Internal
  Property<CachedQueueService> getSharedQueueService();
}
