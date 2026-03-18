/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link StringTransformations}.
 *
 *
 */
public class StringTransformationsUnitTest {
  
  /**
   * Test for {@link StringTransformations#CAPITALIZE}
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
   * Test for {@link StringTransformations#ESCAPE_JAVA}
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
   * Test for {@link StringTransformations#CAPITALIZE}
   */
  @Test
  public void test__Transformations_CAPITALIZE_null() {
    
    assertThrows(NullPointerException.class, () -> StringTransformations.CAPITALIZE.apply(null));
    
  }
  
}
