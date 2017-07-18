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
package de.se_rwth.commons.reflection;

/**
 * Encapsulates exceptions that have been thrown during reflective operations.
 * 
 * @author (last commit) $Author: nazari $
 * @version $Revision: 381 $, $Date: 2012-10-18 16:55:10 +0200 (Do, 18 Okt 2012)
 *          $
 * 
 */
public class ReflectionException extends Exception {
  

  private static final long serialVersionUID = 7666884544454316393L;
  
  /**
   * Factory method for cc.clarc.commons.ReflectionException
   */
  public static final ReflectionException of(Exception e) {
    return new ReflectionException(e);
  }
  
  /**
   * Factory method for cc.clarc.commons.ReflectionException
   */
  public static final ReflectionException of(String message, Exception e) {
    return new ReflectionException(message, e);
  }
  
  /**
   * Constructor for cc.clarc.commons.ReflectionException
   */
  private ReflectionException(Exception e) {
    super(e);
  }
  
  /**
   * Constructor for cc.clarc.commons.ReflectionException
   */
  private ReflectionException(String message, Exception e) {
    super(message, e);
  }
  
}
