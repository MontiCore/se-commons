/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
 * Copyright (c) 2017, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
package de.se_rwth.commons;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;

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
        && CharMatcher.JAVA_LETTER.matches(name.charAt(0))
        && CharMatcher.JAVA_LETTER_OR_DIGIT.matchesAllOf(name));
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
