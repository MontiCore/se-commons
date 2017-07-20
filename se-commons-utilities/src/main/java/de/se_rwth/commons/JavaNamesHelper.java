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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Functions dealing with java names.
 *
 * @author Robert Heim
 */
public class JavaNamesHelper {
  
  public static final String PREFIX_WHEN_WORD_IS_RESERVED = "r__";
  
  /**
   * lowers first char and checks for reserved java-keyword. If it is a keyword
   * the {@link #getNonReservedName(String)} will be called.
   */
  public static String javaAttribute(String in) {
    if (in != null && in.length() > 0) {
      String out = (in.substring(0, 1).toLowerCase() + in.substring(1)).intern();
      return getNonReservedName(out);
    }
    return in;
  }
  
  public JavaNamesHelper() {
  };
  
  public static String getNonReservedName(String name) {
    return JavaReservedWordReplacer.getNonReservedName(name);
  }
  
  /**
   * Class generates non-reserved alternative words for reserved words in Java
   * and makes all returned words intern (so == for Strings is possible!)
   * 
   * @author krahn
   */
  private static class JavaReservedWordReplacer {
    
    private static Set<String> goodNames = null;
    
    /**
     * Returns a non reserved name
     * 
     * @param name should be replaced by a non-reserved name
     * @return non reserved name for name if name is a reserved anme in Java,
     * name otherwise
     */
    public static String getNonReservedName(String name) {
      
      if (goodNames == null) {
        goodNames = new HashSet<String>();
        goodNames.addAll(Arrays.asList(new String[] { "abstract", "continue",
            "for", "new", "switch", "assert", "default", "goto", "package",
            "synchronized", "boolean", "do", "if", "private", "this",
            "break", "double", "implements", "protected", "throw", "byte",
            "else", "import", "public", "throws", "case", "enum",
            "instanceof", "return", "transient", "catch", "extends", "int",
            "short", "try", "char", "final", "interface", "static", "void",
            "class", "finally", "long", "strictfp", "volatile", "const",
            "float", "native", "super", "while" }));
        
      }
      if (name == null) {
        return null;
      }
      else if (goodNames.contains(name))
        return (PREFIX_WHEN_WORD_IS_RESERVED + name).intern();
      else
        return name.intern();
    }
    
  }
  
}
