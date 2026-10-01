/* (c) https://github.com/MontiCore/monticore */
package se.rwth.example;

import de.monticore.gradle.internal.ProgressLoggerService;
import de.monticore.gradle.queue.ICachedQueueTask;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;

/**
 * A Task scheduling its action using the selected workQueue
 */
public abstract class ExampleTask extends DefaultTask implements ICachedQueueTask {

  @Inject
  abstract public WorkerExecutor getWorkerExecutor();

  @Input
  abstract public ListProperty<String> getTaskNames();

  @Input
  @Optional
  abstract public Property<Integer> getWaitSeconds();

  @Input
  @Optional
  abstract public Property<WorkerKind> getWorkerKind();
  
  @Input
  @Optional
  abstract public Property<Boolean> getWithTestService();
  
  @TaskAction
  public void execute() {
    WorkQueue queue;
    switch (getWorkerKind().getOrElse(WorkerKind.NO_ISOLATION)) {
      case NO_ISOLATION:
        queue = getWorkerExecutor().noIsolation();
        break;
      case CL:
        queue = getWorkerExecutor().classLoaderIsolation();
        break;
      case SHARED:
        queue = doGetSharedQueueService().newWorkQueue(getWorkerExecutor(), getServiceRegistry(), getProject().getObjects().fileCollection());
        break;
      default:
        throw new IllegalStateException("Unknown worker kind: " + getWorkerKind().get());
    }

    int waitSeconds = getWaitSeconds().getOrElse(1);
    for (final String name : getTaskNames().get()) {
      final int secondForThisRun = waitSeconds += 2;
      queue.submit(TestAction.class, params -> {
        params.getWaitSeconds().set(secondForThisRun);
        params.getName().set(name);
        params.getPrefix().set("[" + name + "]"); // and set the prefix
        params.getStatsUniqueName().set(name);
        params.getWithTestService().set(this.getWithTestService().orElse(false));
        if (this.getWithTestService().getOrElse(false)) {
          params.getProgressLogger().set(getProgressLoggerService());
        }
      });
    }

  }

  public enum WorkerKind {
    NO_ISOLATION, // no isolation
    CL, // classloader isolation
    SHARED // shared isolation
  }
  
  
  @Internal
  public abstract Property<ProgressLoggerService> getProgressLoggerService();
}
