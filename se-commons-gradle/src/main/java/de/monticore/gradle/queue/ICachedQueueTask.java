/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.queue;

import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

import java.lang.reflect.Proxy;

/**
 * A task with access to a cached queue service.
 * Automatically managed via the {@link CachedQueueServicePlugin}
 */
public interface ICachedQueueTask extends Task {
  
  // Type must be object due to https://github.com/gradle/gradle/issues/17559
  @Internal
  Property<Object> getSharedQueueServiceProperty();
  
  @Internal
  @Deprecated(forRemoval = true)
  default Property<ICachedQueueService> getSharedQueueService() {
    return (Property<ICachedQueueService>) ((Property) getSharedQueueServiceProperty());
  }
  
  default ICachedQueueService doGetSharedQueueService() {
    return ICachedQueueService.asICachedQueueService(getSharedQueueServiceProperty().get());
  }
}
