/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
