/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.isolation;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.Cleaner;

/**
 * This class provides a
 * When no more references to this class exists,
 *  the wrapped closeable/resource will be closes if no
 *  other references to the resource exist.
 * @param <T>
 */
public class SharedCloseable<T extends Closeable> implements AutoCloseable {
  private final Cleaner cleaner = CleanerProvider.getCleaner();

  private final Cleaner.Cleanable cleanable;

  private final T res;

  public SharedCloseable(T res) {
    this.res = res;
    this.cleanable = cleaner.register(this, cleanAction(res));
    CleanerProvider.notifyResourceUse(res);
  }

  @Override
  public void close() {
    System.err.println("Got close request");
    this.cleanable.clean();
  }

  public T get() {
    return this.res;
  }

  private static Runnable cleanAction(Closeable closeable) {
    return () -> {
      if (CleanerProvider.notifyResourceClean(closeable)) {
        // This appears to be the last use of the resource
        // => actually close the resource
        try {
          closeable.close();
        } catch (IOException ignored) { }
      }
    };
  }
}
