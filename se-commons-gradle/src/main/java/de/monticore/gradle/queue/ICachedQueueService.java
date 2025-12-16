package de.monticore.gradle.queue;

import org.gradle.api.file.FileCollection;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

/**
 * @since 7.9.0
 */
public interface ICachedQueueService {
  
  /**
   * Construct a new WorkQueue
   *
   * @param workerExecutor        the worker executor to use
   * @param extraClasspathElement the classpath elements to use
   * @return a new {@link WorkQueue}
   */
  WorkQueue newWorkQueue(WorkerExecutor workerExecutor, FileCollection extraClasspathElement);
  
  /**
   * Returns the tracked stats as a serialized JSON string.
   * @see CachedIsolationStats
   * @return a serialized JSON string of the stats
   */
  String getStats();
}
