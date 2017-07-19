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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for {@link StringMatchers}
 *
 *
 */
public class StringMatchersUnitTest {
  
  /**
   * Testcase getLevenstheinDistance/equivalenceClassTest
   */
  @Test
  public void test__getLevenstheinDistance_equivalenceClassTest() {
    
    String a, b;
    int distance;
    
    a = "";
    b = "Hello";
    distance = StringMatchers.getLevenshteinDistance(a, b);
    assertThat(distance, is(5));
    
    a = "Hallo";
    b = "";
    distance = StringMatchers.getLevenshteinDistance(a, b);
    assertThat(distance, is(5));
    
    a = "Hallo";
    b = "Hello";
    distance = StringMatchers.getLevenshteinDistance(a, b);
    assertThat(distance, is(1));
    
    a = "Hallo";
    b = "hallo";
    distance = StringMatchers.getLevenshteinDistance(a, b);
    assertThat(distance, is(1));
    
    a = "Hallo";
    b = "Hallo World";
    distance = StringMatchers.getLevenshteinDistance(a, b);
    assertThat(distance, is(6));
    
    a = "Hallo";
    b = "Hallo";
    distance = StringMatchers.getLevenshteinDistance(a, b);
    assertThat(distance, is(0));
    
    a = "Hallo";
    b = "Hallu";
    distance = StringMatchers.getLevenshteinDistance(a, b);
    assertThat(distance, is(1));
    
    a = "Hallo";
    b = "Jallo";
    distance = StringMatchers.getLevenshteinDistance(a, b);
    assertThat(distance, is(1));
    
    a = "";
    b = " ";
    distance = StringMatchers.getLevenshteinDistance(a, b);
    assertThat(distance, is(1));
    
  }
  
  /**
   * Test for {@link StringMatchers#matchesWildcardPattern(String, String)}
   */
  @Test
  public void test__matchesWildcardPattern() {
    
    assertFalse(StringMatchers.matchesWildcardPattern(" ", ""));
    assertFalse(StringMatchers.matchesWildcardPattern("A", ""));
    assertFalse(StringMatchers.matchesWildcardPattern("Ab", ""));
    assertFalse(StringMatchers.matchesWildcardPattern("A b", ""));
    
    assertTrue(StringMatchers.matchesWildcardPattern("", ""));
    assertTrue(StringMatchers.matchesWildcardPattern("", "*"));
    assertTrue(StringMatchers.matchesWildcardPattern("A", "*"));
    assertTrue(StringMatchers.matchesWildcardPattern("Ab", "*"));
    assertTrue(StringMatchers.matchesWildcardPattern("A b", "*"));
    
    assertTrue(StringMatchers.matchesWildcardPattern("Some longer sentence.", "Some*sentence*"));
    assertTrue(StringMatchers.matchesWildcardPattern("Some longer sentence.", "*Some*sentence*"));
    assertTrue(StringMatchers.matchesWildcardPattern("Some longer sentence.", "**Some**se*ntence***"));
    assertTrue(StringMatchers.matchesWildcardPattern("Some longer sentence.", "Some*sentence."));
    assertTrue(StringMatchers.matchesWildcardPattern("Some longer sentence.", "*Some*sentence."));
    
  }
  
  /**
   * Test for {@link StringMatchers#matchesWildcardPattern(String, String)}
   */
  @Test
  public void test__isValidQualifier() {
    
    assertTrue(StringMatchers.isValidQualifier("a"));
    assertTrue(StringMatchers.isValidQualifier("a.a"));
    assertTrue(StringMatchers.isValidQualifier("Abc"));
    assertTrue(StringMatchers.isValidQualifier("Ab123"));
    assertTrue(StringMatchers.isValidQualifier("a_a.aaa3.2.a1.21"));
    assertTrue(StringMatchers.isValidQualifier("a"));
    
    assertFalse(StringMatchers.isValidQualifier(null));
    assertFalse(StringMatchers.isValidQualifier(""));
    assertFalse(StringMatchers.isValidQualifier("1"));
    assertFalse(StringMatchers.isValidQualifier("12"));
    assertFalse(StringMatchers.isValidQualifier("1abc"));
    assertFalse(StringMatchers.isValidQualifier("."));
    assertFalse(StringMatchers.isValidQualifier("a."));
    assertFalse(StringMatchers.isValidQualifier(".a"));
    assertFalse(StringMatchers.isValidQualifier(".a."));
    
  }

}
