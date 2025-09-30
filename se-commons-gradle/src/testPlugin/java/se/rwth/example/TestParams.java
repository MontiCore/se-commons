/* (c) https://github.com/MontiCore/monticore */
package se.rwth.example;

import de.monticore.gradle.queue.CachedIsolatedWorkQueue;
import org.gradle.api.provider.Property;

public interface TestParams extends CachedIsolatedWorkQueue.WorkQueueParameters {
  Property<String> getName();
  Property<Integer> getWaitSeconds();
}
