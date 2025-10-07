/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.queue;

import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.internal.instantiation.InstantiatorFactory;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutionException;

import java.util.UUID;

/**
 * {@link WorkQueue} implementation using an isolated classloader,
 * but re-using these.
 * <p>
 * Retrieve a new instance using the {@link CachedQueueService}
 */
public class CachedIsolatedWorkQueue implements WorkQueue {

  protected final WorkQueue backingQueue;

  protected final Provider<CachedQueueService> cachedQueueService;

  protected final InstantiatorFactory instantiatorFactory;
  protected final ServiceRegistry serviceRegistry;

  protected final FileCollection classpath;

  protected CachedIsolatedWorkQueue(WorkQueue backingQueue, InstantiatorFactory instantiatorFactory,
                                    ServiceRegistry services,
                                    Provider<CachedQueueService> cachedQueueService,
                                    FileCollection classpath) {
    this.backingQueue = backingQueue;
    this.cachedQueueService = cachedQueueService;

    this.serviceRegistry = services;
    this.instantiatorFactory = instantiatorFactory;
    this.classpath = classpath;
  }

  @Override
  public <T extends WorkParameters> void submit(Class<? extends WorkAction<T>> workActionClass,
                                                Action<? super T> parameterAction) {
    final UUID uuid = UUID.randomUUID();

    cachedQueueService.get().register(uuid, workActionClass, parameterAction, this.instantiatorFactory, this.serviceRegistry, classpath);
    backingQueue.submit(WAction.class, _p -> {
      _p.getUUID().set(uuid);
      _p.getQueueService().set(cachedQueueService);
    });
  }

  @Override
  public void await() throws WorkerExecutionException {
    this.backingQueue.await();
  }


  /**
   * WorkAction for our backing queue
   */
  public static abstract class WAction implements WorkAction<WrappingParams> {

    @Override
    public void execute() {
      try {
        getParameters().getQueueService().get().doExecuteWorkAction(getParameters().getUUID().get());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public interface WrappingParams extends WorkParameters {
    Property<UUID> getUUID();

    Property<CachedQueueService> getQueueService();
  }

  public interface WorkQueueParameters extends WorkParameters {

    /**
     * @return The prefix used for the logger
     */
    Property<String> getPrefix();

    /**
     * @return A unique name used for reporting
     */
    Property<String> getStatsUniqueName();
  }
}
