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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.Test;

/**
 * Unit test for {@link Reflection}.
 * 
 * @author (last commit) $Author: nazari $
 * @version $Revision: 381 $, $Date: 2015-09-04 14:18:19 +0200 (Fr, 04 Sep 2015) $
 * 
 */
public class ReflectionUnitTest {
  
  @Test
  public void test__getAnnotatedFields() {
    Reflection.getAnnotatedFields(HashSet.class, Override.class);
  }
  
  @Test
  public void test__getSuperclassTypeParameter_noParameters() {
    
    @SuppressWarnings("rawtypes") HashMap parameterizedType = new HashMap() {
      private static final long serialVersionUID = 1L;
    };
    Class<?> clazz = Reflection.getSuperclassTypeParameter(parameterizedType.getClass());
    assertEquals(null, clazz);
    
  }
  
  @Test
  public void test__getSuperclassTypeParameter_oneParameter() {
    
    ArrayList<String> parameterizedType = new ArrayList<String>() {
      private static final long serialVersionUID = 1L;
    };
    Class<?> clazz = Reflection.getSuperclassTypeParameter(parameterizedType.getClass());
    assertEquals(String.class, clazz);
    
  }
  
  @Test
  public void test__getSuperclassTypeParameter_twoParameters() {
    
    HashMap<String, Integer> parameterizedType = new HashMap<String, Integer>() {
      private static final long serialVersionUID = 1L;
    };
    Class<?> clazz = Reflection.getSuperclassTypeParameter(parameterizedType.getClass());
    assertEquals(String.class, clazz);
    
  }
  
}
