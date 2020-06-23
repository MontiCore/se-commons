/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import com.google.common.base.Objects;

import static com.google.common.base.CharMatcher.javaLetter;
import static com.google.common.base.CharMatcher.javaLetterOrDigit;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A symbol represents a name. Symbols can be used as markers in collections
 * where names based on Strings might collide with other values.
 *
 *
 */
public final class Symbol {
  
  /**
   * Factory method for {@link Symbol}.
   */
  public static final Symbol of(String name) {
    return new Symbol(name);
  }
  
  private final String name;
  
  /**
   * Constructor for de.se_rwth.commons.Symbol
   */
  private Symbol(String name) {
    checkNotNull(name);
    checkArgument(name.length() > 0
        && javaLetter().matches(name.charAt(0))
        && javaLetterOrDigit().matchesAllOf(name));
    this.name = checkNotNull(name);
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    }
    if (this == object) {
      return true;
    }
    if (this.getClass() != object.getClass()) {
      return false;
    }
    Symbol that = (Symbol) object;
    return Objects.equal(this.name, that.name);
  }
  
  /**
   * @return name
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return Objects.hashCode(this.name);
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getName();
  }
  
}
