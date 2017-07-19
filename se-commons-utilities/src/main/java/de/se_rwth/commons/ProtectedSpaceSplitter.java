/*
 * ******************************************************************************
 * MontiCore Language Workbench
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

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * An alternative to {@link Splitter} that supports the definition of protected
 * space demarked by delimiters, e.g., "Do not split here"
 *
 *
 */
public class ProtectedSpaceSplitter {
  
  /**
   * Factory method for {@link ProtectedSpaceSplitter}.
   */
  public static final ProtectedSpaceSplitter on(char separator) {
    return new ProtectedSpaceSplitter(Splitter.on(separator));
  }
  
  /**
   * Factory method for {@link ProtectedSpaceSplitter}.
   */
  public static final ProtectedSpaceSplitter on(CharMatcher separatorMatcher) {
    return new ProtectedSpaceSplitter(Splitter.on(separatorMatcher));
  }
  
  private final Splitter splitter;
  
  private CharMatcher leftDelimiter;
  private CharMatcher rightDelimiter;
  private boolean includeDelimiter = false;
  private boolean ommitEmptyStrings = false;
  
  /**
   * Constructor for de.se_rwth.commons.ProtectedSpaceSplitter
   */
  public ProtectedSpaceSplitter(Splitter splitter) {
    this.splitter = splitter;
  }
  
  /**
   * If true, the delimiters are included in the part representing the
   */
  public final ProtectedSpaceSplitter includeDelimiters(boolean includeDelimiter) {
    this.includeDelimiter = includeDelimiter;
    return this;
  }
  
  /**
   * @see Splitter
   */
  public final ProtectedSpaceSplitter omitEmptyStrings() {
    this.ommitEmptyStrings = true;
    return this;
  }
  
  /**
   * @see Splitter
   */
  public final Iterable<String> split(String string) {

    ImmutableList.Builder<String> parts = ImmutableList.builder();
    
    boolean inProtectedSection = false;
    int begin = 0;
    
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if (this.leftDelimiter.matches(c) && !inProtectedSection) {
        inProtectedSection = true;
        if (i > 0) {
          parts.addAll(this.splitter.split(string.substring(begin, i - 1)));
        }
        begin = i + (this.includeDelimiter ? 0 : 1);
      }
      else if (this.rightDelimiter.matches(c) && inProtectedSection) {
        inProtectedSection = false;
        parts.add(string.substring(begin, i + (this.includeDelimiter ? 1 : 0)));
        begin = i + 1;
      }
      else if (i == string.length() - 1 && !inProtectedSection) {
        parts.addAll(this.splitter.split(string.substring(begin, i + 1)));
      }
    }
    
    if (this.ommitEmptyStrings) {
      Iterable<String> partsWithEmptyStrings = parts.build();
      parts = ImmutableList.builder();
      for (String part : partsWithEmptyStrings) {
        if (!part.isEmpty()) {
          parts.add(part);
        }
      }
    }
    
    return parts.build();
  }
  
  /**
   * @see Splitter
   */
  public final ProtectedSpaceSplitter trimResults() {
    this.splitter.trimResults();
    return this;
  }
  
  /**
   * @see Splitter
   */
  public final ProtectedSpaceSplitter trimResults(CharMatcher trimmer) {
    this.splitter.trimResults(trimmer);
    return this;
  }
  
  /**
   * Defines the delimiter of protected space.
   */
  public final ProtectedSpaceSplitter withProtectionDelimiter(char delimiter) {
    this.leftDelimiter = CharMatcher.is(delimiter);
    this.rightDelimiter = CharMatcher.is(delimiter);
    return this;
  }
  
  /**
   * Defines the delimiter of protected space.
   */
  public final ProtectedSpaceSplitter withProtectionDelimiter(CharMatcher delimiter) {
    this.leftDelimiter = delimiter;
    this.rightDelimiter = delimiter;
    return this;
  }
  
  /**
   * Defines the delimiters of protected space.
   */
  public final ProtectedSpaceSplitter withProtectionDelimiters(char leftDelimiter, char rightDelimiter) {
    this.leftDelimiter = CharMatcher.is(leftDelimiter);
    this.rightDelimiter = CharMatcher.is(rightDelimiter);
    return this;
  }
  
  /**
   * Defines the delimiters of protected space.
   */
  public final ProtectedSpaceSplitter withProtectionDelimiters(CharMatcher leftDelimiter, CharMatcher rightDelimiter) {
    this.leftDelimiter = leftDelimiter;
    this.rightDelimiter = rightDelimiter;
    return this;
  }
  
}
