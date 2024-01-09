/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.io;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

/**
 * A PrintStream which can be set as the System.err/System.out PrintStream.
 * It allows a different redirection target per thread
 */
public class PrintStreamThreadProxy extends PrintStream {

  /**
   * The per-thread PrintStream
   */
  protected final ThreadLocal<PrintStream> redirectTarget;
  protected final PrintStream original;

  public PrintStreamThreadProxy(PrintStream original) {
    super(original);
    redirectTarget = ThreadLocal.withInitial(this::getOriginal);
    this.original = original;
  }

  public void setRedirect(PrintStream stream) {
    this.redirectTarget.set(stream);
  }

  protected PrintStream getReal() {
    return this.redirectTarget.get();
  }

  public PrintStream getOriginal() {
    return original;
  }

  public void reset() {
    this.redirectTarget.remove();
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
