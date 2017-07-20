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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit test for {@link StringTransformations}.
 *
 *
 */
public class StringTransformationsUnitTest {
  
  /**
   * Test for {@link StringMatchers.Transformations#CAPITALIZE}
   */
  @Test
  public void test__Transformations_CAPITALIZE() {
    
    String result;
    
    result = StringTransformations.CAPITALIZE.apply("");
    assertThat(result, is(""));
    
    result = StringTransformations.CAPITALIZE.apply("a");
    assertThat(result, is("A"));
    
    result = StringTransformations.CAPITALIZE.apply("ab");
    assertThat(result, is("Ab"));
    
    result = StringTransformations.CAPITALIZE.apply(" a");
    assertThat(result, is(" a"));
    
  }
  
  /**
   * Test for {@link StringMatchers.Transformations#ESCAPE_JAVA}
   */
  @Test
  public void test__Transformations_ESCAPE_JAVA() {
    
    String result;
    
    result = StringTransformations.ESCAPE_JAVA.apply("");
    assertThat(result, is(""));
    
    result = StringTransformations.ESCAPE_JAVA.apply("public void foo() {}");
    assertThat(result, is("public void foo() {}"));
    
    result = StringTransformations.ESCAPE_JAVA.apply("\"");
    assertThat(result, is("\\\""));
    
    result = StringTransformations.ESCAPE_JAVA.apply("A line.\nAnother line.");
    assertThat(result, is("A line.\\nAnother line."));
    
  }
  
  /**
   * Test for {@link StringMatchers.Transformations#CAPITALIZE}
   */
  @Test(expected = NullPointerException.class)
  public void test__Transformations_CAPITALIZE_null() {
    
    StringTransformations.CAPITALIZE.apply(null);
    
  }
  
}
