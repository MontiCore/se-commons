/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.queue;


import com.google.gson.Gson;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Statistics collector for the cached isolated worker.
 *
 */
public class CachedIsolationStats {

  protected List<Event> events = Collections.synchronizedList(new ArrayList<>());

  void track(EventKind kind, @Nullable UUID uuid, int semaphoreMax, List<CachedQueueService.IIsolationData> runners) {
    track(kind, uuid, semaphoreMax, runners, null);
  }

  void track(EventKind kind, @Nullable UUID uuid, int semaphoreMax, List<CachedQueueService.IIsolationData> runners, String reason) {
    Event event = new Event();
    event.systemInfo = new SystemInfo();
    event.systemInfo.semaphoreMax = semaphoreMax;
    event.kind = kind;
    event.runner = uuid;
    event.existingRunnerList = createRunnerList(runners);
    event.reason = reason;
    events.add(event);
  }

  public String asJson(Gson gson) {
    return gson.toJson(new ArrayList<>(this.events));
  }

  protected List<ExistingRunner> createRunnerList(List<CachedQueueService.IIsolationData> runners) {
    return runners.stream().map(r -> {
      ExistingRunner existingRunner = new ExistingRunner();
      existingRunner.lastRun = r.getLastRun();
      existingRunner.uuid = r.getUUID();
      existingRunner.running = r.isRunning();
      return existingRunner;
    }).collect(Collectors.toList());
  }

  // The following classes are serialized as json objects

  @SuppressWarnings("unused")
  protected static class Event {
    long time = System.currentTimeMillis();
    SystemInfo systemInfo;
    EventKind kind;
    List<ExistingRunner> existingRunnerList;
    UUID runner;
    String reason;
  }

  enum EventKind {
    REUSE,
    CREATE,
    CLEANUP,
    START,
    DONE,
    /**
     * The project build is done
     */
    BUILT_DONE
  }


  @SuppressWarnings("unused")
  protected static class SystemInfo {
    final long freeMemory = Runtime.getRuntime().freeMemory();
    final long totalMemory = Runtime.getRuntime().totalMemory();
    final long maxMemory = Runtime.getRuntime().maxMemory();
    final long availableProcessors = Runtime.getRuntime().availableProcessors();
    int semaphoreMax;
  }


  protected static class ExistingRunner {
    UUID uuid;
    boolean running;
    long lastRun;
  }
}
