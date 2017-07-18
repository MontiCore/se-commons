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
