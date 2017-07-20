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

import java.util.Optional;

/**
 * Represents the truth values of a ternary logic value, that is, true, false
 * and undefined.
 *
 *
 */
public enum TernaryLogicValue {
  
  TRUE(true), FALSE(false), UNDEFINED(null);
  
  private final Boolean booleanValue;
  
  /**
   * Constructor for cc.clarc.lang.architecture.symboltable.TernaryLogicValue
   */
  private TernaryLogicValue(Boolean booleanValue) {
    this.booleanValue = booleanValue;
  }
  
  public final Optional<Boolean> asBoolean() {
    return Optional.ofNullable(this.booleanValue);
  }
  
}
