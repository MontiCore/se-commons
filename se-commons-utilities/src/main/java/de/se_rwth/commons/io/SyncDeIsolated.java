/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.io;

/**
 * The IsolatedURLClassLoader SHALL not isolate this class.
 * Instead, it should delegate to its contextClassLoader when this class is loaded.
 */
public final class SyncDeIsolated {
  
  /**
   * Execute a section of block synchronized between isolated classloaders
   * This is useful for e.g. script initializing,
   * to avoid class loaders blocking each other when trying to load from the same backing JarFile
   */
  public static void run(Runnable r) {
    synchronized (SyncDeIsolated.class) {
      r.run();
    }
  }
}
