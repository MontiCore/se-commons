/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
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
