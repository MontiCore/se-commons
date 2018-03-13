/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;


/**
 * Common precomputed {@link Splitter}s.
 *
 *
 */
public final class Splitters {
  
  public static final Splitter COMMA = Splitter.on(',').trimResults();
  public static final Splitter DOT = Splitter.on('.');
  public static final Splitter QUALIFIED_NAME_DELIMITERS = Splitter.on(CharMatcher.anyOf(".<,>"));
  public static final Splitter SEMICOLON = Splitter.on(';');
  public static final Splitter WILDCARD = Splitter.on('*');
  
  /**
   * Private constructor permitting manual instantiation.
   */
  private Splitters() {
  }

}
