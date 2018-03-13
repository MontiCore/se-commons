/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import com.google.common.base.Joiner;


/**
 * Common precomputed {@link Joiner}s.
 *
 *
 */
public final class Joiners {
  
  public static final Joiner DOT = Joiner.on('.').skipNulls();
  public static final Joiner EMPTY = Joiner.on("").skipNulls();
  public static final Joiner COMMA = Joiner.on(",").skipNulls();
  public static final Joiner SEMICOLON = Joiner.on(";").skipNulls();
  
  /**
   * Private constructor permitting manual instantiation.
   */
  private Joiners() {
  }
  
}
