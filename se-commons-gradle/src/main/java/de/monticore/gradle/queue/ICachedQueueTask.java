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
    return (Property<ICachedQueueService>)(  (Property)getSharedQueueServiceProperty());
  }
  
  default ICachedQueueService doGetSharedQueueService() {
    Object sharedQueueService = getSharedQueueServiceProperty().get();
    if (sharedQueueService instanceof ICachedQueueService) {
      return (ICachedQueueService) getSharedQueueServiceProperty().get();
    }
    // Gradle might load the plugin into multiple classloaders (once per subproject where it is applied)
    // If that is the case, ICachedQueueService (from CL 1) is not castable to ICachedQueueService (from CL 2)
    // As we only expose the ICachedQueueService, we can create a proxy between both classloaders
    return (ICachedQueueService) Proxy.newProxyInstance(CachedQueueService.class.getClassLoader(),
        new Class[] { ICachedQueueService.class }, (proxy, method, args) -> {
          Object realObject = getSharedQueueServiceProperty().get();
          return realObject.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(realObject, args);
        });
  }
  
}
