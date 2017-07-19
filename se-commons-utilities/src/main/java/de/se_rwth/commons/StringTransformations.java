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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

/**
 * {@link Function}s that transform {@link String}s.
 *
 */
public final class StringTransformations {
  
  /**
   * Capitalizes a given String.
   */
  public static final Function<String, String> CAPITALIZE = new Function<String, String>() {
    @Override
    public String apply(String string) {
      if (!string.isEmpty()) {
        string = string.substring(0, 1).toUpperCase().concat(string.substring(1));
      }
      return string;
    }
  };
  
  /**
   * Escapes java source code.
   */
  private static final Escaper javaEscaper = Escapers.builder()
      .addEscape('\b', "\\b")
      .addEscape('\f', "\\f")
      .addEscape('\n', "\\n")
      .addEscape('\r', "\\r")
      .addEscape('\t', "\\t")
      .addEscape('\"', "\\\"")
      .addEscape('\\', "\\\\")
      .addEscape('\'', "\\'")
      .build();
  
  /**
   * Capitalizes a given String.
   */
  public static final Function<String, String> ESCAPE_JAVA = new Function<String, String>() {
    @Override
    public String apply(String string) {
      return javaEscaper.escape(string);
    }
  };
  
  /**
   * Uncapitalizes a given String.
   */
  public static final Function<String, String> UNCAPITALIZE = new Function<String, String>() {
    @Override
    public String apply(String string) {
      if (!string.isEmpty()) {
        string = string.substring(0, 1).toLowerCase().concat(string.substring(1));
      }
      return string;
    }
  };
  
  /**
   * Removes leading and trailing dots from the given String.
   */
  public static final Function<String, String> TRIM_DOT = new Function<String, String>() {
    @Override
    @Nullable
    public String apply(@Nullable String string) {
      return StringMatchers.DOT.trimFrom(string);
    }
  };
  
  /**
   * Removes leading and trailing whitespace from the given String.
   */
  public static final Function<String, String> TRIM_WHITESPACE = new Function<String, String>() {
    @Override
    @Nullable
    public String apply(@Nullable String string) {
      return CharMatcher.WHITESPACE.trimFrom(string);
    }
  };
  
  /**
   * Transforms the given String into a valid Java identifier.
   */
  public static final Function<String, String> TO_IDENTIFIER = new Function<String, String>() {
    @Override
    public String apply(String string) {
      return StringMatchers.DOT.replaceFrom(string, '_');
    }
  };
  
  /**
   * Transforms the given Object into a String.
   */
  public static final Function<Object, String> TO_STRING = new Function<Object, String>() {
    @Override
    @Nullable
    public String apply(@Nullable Object object) {
      return object == null ? "" : object.toString();
    }
  };
  
  /**
   * Capitalizes the given String.
   */
  public static final String capitalize(String string) {
    return CAPITALIZE.apply(string);
  }
  
  /**
   * Returns the given string escaped for Java.
   */
  public static String escapeJava(String javaCode) {
    return ESCAPE_JAVA.apply(javaCode);
  }
  
  /**
   * Transforms the given String into a valid Java identifier.
   */
  public static final String toIdentifier(String string) {
    return TO_IDENTIFIER.apply(string);
  }
  
  /**
   * Uncapitalizes the given String.
   */
  public static final String uncapitalize(String string) {
    return UNCAPITALIZE.apply(string);
  }
  
  /**
   * Wraps the given String according to the given max length.
   */
  public static String wrap(String string, int maxLength, boolean wrapLongWords) {
    
    checkNotNull(string);
    checkArgument(maxLength > 0);
    
    final String newLineStr = "\n";
    
    int inputLineLength = string.length();
    int offset = 0;
    StringBuffer wrappedText = new StringBuffer(inputLineLength + 32);
    
    while ((inputLineLength - offset) > maxLength) {
      if (string.charAt(offset) == ' ') {
        offset++;
        continue;
      }
      int spaceToWrapAt = string.lastIndexOf(' ', maxLength + offset);
      
      if (spaceToWrapAt >= offset) {
        wrappedText.append(string.substring(offset, spaceToWrapAt));
        wrappedText.append(newLineStr);
        offset = spaceToWrapAt + 1;
      }
      
      else {
        if (wrapLongWords) {
          wrappedText.append(string.substring(offset, maxLength + offset));
          wrappedText.append(newLineStr);
          offset += maxLength;
        }
        else {
          spaceToWrapAt = string.indexOf(' ', maxLength + offset);
          if (spaceToWrapAt >= 0) {
            wrappedText.append(string.substring(offset, spaceToWrapAt));
            wrappedText.append(newLineStr);
            offset = spaceToWrapAt + 1;
          }
          else {
            wrappedText.append(string.substring(offset));
            offset = inputLineLength;
          }
        }
      }
    }
    
    wrappedText.append(string.substring(offset));
    
    return wrappedText.toString();
  }
  
  public StringTransformations() {
  }
  
}
