/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.io;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.Cleaner;

/**
 * When no more references to this class exists,
 *  the wrapped closeable/resource will be closes if no
 *  other references to the resource exist.
 * @param <T> a {@link Closeable}
 */
public class SharedCloseable<T extends Closeable> implements AutoCloseable {
  private final Cleaner cleaner = CleanerProvider.getCleaner();

  private final Cleaner.Cleanable cleanable;

  // The actual Closeable
  private final T res;

  public SharedCloseable(T res) {
    this.res = res;
    this.cleanable = cleaner.register(this, cleanAction(res));
    CleanerProvider.notifyResourceUse(res);
  }

  /**
   * Notifies the resource, that it can be closed.
   * The resource is only freed if this was the only active usage of it
   */
  @Override
  public void close() {
    this.cleanable.clean();
  }

  /**
   * @return the actual resource
   */
  public T get() {
    return this.res;
  }

  /**
   * The action performed when a {@link SharedCloseable} instance is no longer in memory
   * @param closeable the actual resource, kept in memory due to being associated with the Runnable
   * @return a Runnable which is detached from the instance of its {@link SharedCloseable}
   */
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
