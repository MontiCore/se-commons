/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.io;

import javax.annotation.Nonnull;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * modified {@link PrintStream} adding a prefix when printing a new line
 */
public class PrefixStream extends PrintStream {
  protected final String prefix;

  public PrefixStream(OutputStream out, String prefix) {
    super(out);
    this.prefix = prefix;
  }

  @Override
  public void println(String x) {
    super.println(prefix + x);
  }

  @Override
  public PrintStream printf(@Nonnull String format, Object... args) {
    return super.printf(prefix + format, args);
  }
}