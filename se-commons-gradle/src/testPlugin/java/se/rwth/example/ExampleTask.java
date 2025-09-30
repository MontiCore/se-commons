/* (c) https://github.com/MontiCore/monticore */
package se.rwth.example;

import de.monticore.gradle.queue.ICachedQueueTask;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
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
        queue = getSharedQueueService().get().newWorkQueue(getWorkerExecutor(), getProject().getObjects().fileCollection());
        break;
      default:
        throw new IllegalStateException("Unknown worker kind: " + getWorkerKind().get());
    }

    int waitSeconds = getWaitSeconds().getOrElse(1);
    for (final String name : getTaskNames().get()) {
      final int secondForThisRun = waitSeconds += 2;
      queue.submit(TestAction.class, task -> {
        task.getWaitSeconds().set(secondForThisRun);
        task.getName().set(name);
        task.getPrefix().set("[" + name + "]"); // and set the prefix
        task.getStatsUniqueName().set(name);
      });
    }

  }

  public enum WorkerKind {
    NO_ISOLATION, // no isolation
    CL, // classloader isolation
    SHARED // shared isolation
  }
}
