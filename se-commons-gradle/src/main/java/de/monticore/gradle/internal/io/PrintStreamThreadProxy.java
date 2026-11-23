/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.io;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

/**
 * A PrintStream which can be set as the System.err/System.out PrintStream.
 * It allows a different redirection target per thread.
 * <p>
 * In addition, a per-thread prefix can be set via {@link #setThreadPrefix(String)}.
 * This prefix is shared between all instances of this proxy (of the same classloader), i.e.,
 * it is still applied in case System.err/System.out is replaced (e.g., by Gradle) and a new proxy is
 * installed while the thread is running.
 */
public class PrintStreamThreadProxy extends PrintStream {

  /**
   * The per-thread prefix, shared between all proxy instances
   */
  protected static final ThreadLocal<String> THREAD_PREFIX = new ThreadLocal<>();

  /**
   * The per-thread PrintStream (explicitly set via {@link #setRedirect(PrintStream)})
   */
  protected final ThreadLocal<PrintStream> redirectTarget = new ThreadLocal<>();

  /**
   * The per-thread {@link PrefixStream} for the {@link #THREAD_PREFIX} (cached)
   */
  protected final ThreadLocal<PrefixStream> prefixStream = new ThreadLocal<>();

  protected final PrintStream original;

  public PrintStreamThreadProxy(PrintStream original) {
    super(original);
    this.original = original;
  }

  /**
   * Set the prefix for all outputs of the current thread (for all proxies)
   *
   * @param prefix the prefix, or null to remove it
   */
  public static void setThreadPrefix(@Nullable String prefix) {
    if (prefix == null) {
      THREAD_PREFIX.remove();
    } else {
      THREAD_PREFIX.set(prefix);
    }
  }

  /**
   * Removes the prefix of the current thread (for all proxies)
   */
  public static void resetThreadPrefix() {
    THREAD_PREFIX.remove();
  }

  public void setRedirect(PrintStream stream) {
    this.redirectTarget.set(stream);
  }

  protected PrintStream getReal() {
    PrintStream redirect = this.redirectTarget.get();
    if (redirect != null) {
      return redirect;
    }
    String prefix = THREAD_PREFIX.get();
    if (prefix == null) {
      return this.original;
    }
    PrefixStream stream = this.prefixStream.get();
    if (stream == null || !prefix.equals(stream.getPrefix())) {
      stream = new PrefixStream(this.original, prefix);
      this.prefixStream.set(stream);
    }
    return stream;
  }

  public PrintStream getOriginal() {
    return original;
  }

  /**
   * Resets the redirection of the current thread (for this proxy)
   */
  public void reset() {
    this.redirectTarget.remove();
    this.prefixStream.remove();
  }

  @Override
  public void flush() {
    getReal().flush();
  }

  @Override
  public void close() {
    getReal().close();
  }

  @Override
  public boolean checkError() {
    return getReal().checkError();
  }

  @Override
  public void write(int b) {
    getReal().write(b);
  }

  @Override
  public void write(@Nonnull byte[] buf, int off, int len) {
    getReal().write(buf, off, len);
  }

  @Override
  public void write(byte[] buf) throws IOException {
    getReal().write(buf);
  }

  @Override
  public void println(String s) {
    getReal().println(s);
  }

  @Override
  public void println(Object x) {
    getReal().println(x);
  }

  @Override
  public void println() {
    getReal().println();
  }

  @Override
  public void println(boolean x) {
    getReal().println(x);
  }

  @Override
  public void println(char x) {
    getReal().println(x);
  }

  @Override
  public void println(int x) {
    getReal().println(x);
  }

  @Override
  public void println(long x) {
    getReal().println(x);
  }

  @Override
  public void println(float x) {
    getReal().println(x);
  }

  @Override
  public void println(double x) {
    getReal().println(x);
  }

  @Override
  public void println(@Nonnull char[] x) {
    getReal().println(x);
  }

  @Override
  public PrintStream printf(@Nonnull String format, Object... args) {
    return getReal().printf(format, args);
  }

  @Override
  public PrintStream printf(Locale l, @Nonnull String format, Object... args) {
    return getReal().printf(l, format, args);
  }
}
