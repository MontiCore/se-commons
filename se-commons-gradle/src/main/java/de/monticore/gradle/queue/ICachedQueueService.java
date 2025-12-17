package de.monticore.gradle.queue;

import org.gradle.api.file.FileCollection;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import java.lang.reflect.Proxy;

/**
 * @since 7.9.0
 */
public interface ICachedQueueService {
  
  /**
   * Construct a new WorkQueue
   *
   * @param workerExecutor the worker executor to use
   * @param extraClasspathElement the classpath elements to use
   * @return a new {@link WorkQueue}
   */
  WorkQueue newWorkQueue(WorkerExecutor workerExecutor, FileCollection extraClasspathElement);
  
  /**
   * Returns the tracked stats as a serialized JSON string.
   *
   * @return a serialized JSON string of the stats
   * @see CachedIsolationStats
   */
  String getStats();
  
  void setMaxConcurrentMC(int maxParallelMC);
  
  void setCloseThreshold(long closeThreshold);
  
  static ICachedQueueService asICachedQueueService(Object sharedQueueService) {
    if (sharedQueueService instanceof ICachedQueueService) {
      return (ICachedQueueService) sharedQueueService;
    }
    // Gradle might load the plugin into multiple classloaders (once per subproject where it is applied)
    // If that is the case, ICachedQueueService (from CL 1) is not castable to ICachedQueueService (from CL 2)
    // As we only expose the ICachedQueueService, we can create a proxy between both classloaders
    return (ICachedQueueService) Proxy.newProxyInstance(CachedQueueService.class.getClassLoader(),
        new Class[] { ICachedQueueService.class },
        (proxy, method, args) -> sharedQueueService.getClass()
            .getMethod(method.getName(), method.getParameterTypes())
            .invoke(sharedQueueService, args));
  }
  
}
