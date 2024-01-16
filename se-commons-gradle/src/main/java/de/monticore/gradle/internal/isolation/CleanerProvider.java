/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.isolation;

import java.io.Closeable;
import java.lang.ref.Cleaner;
import java.util.WeakHashMap;

/**
 * This class should be shared, even between isolated classloaders
 */
public class CleanerProvider {
  private final static Cleaner CLEANER = Cleaner.create();

  /**
   * @return a shared Cleaner
   */
  public static Cleaner getCleaner() {
    return CLEANER;
  }

  private final static WeakHashMap<Closeable, Integer> RESOURCE_MAP = new WeakHashMap<>();

  /**
   * Track the usages of a closeable
   */
  public static synchronized void notifyResourceUse(Closeable c) {
    RESOURCE_MAP.put(c, RESOURCE_MAP.getOrDefault(c, 0) + 1);
  }

  /**
   * @return true if the last usage has finished
   */
  public static synchronized boolean notifyResourceClean(Closeable c) {
    Integer uses = RESOURCE_MAP.get(c);
    if (uses > 1) {
      RESOURCE_MAP.put(c, uses - 1);
      return false;
    } else {
      RESOURCE_MAP.remove(c);
      return true;
    }
  }

}
