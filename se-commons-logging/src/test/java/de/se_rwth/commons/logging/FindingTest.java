/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.SourcePositionBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"SimplifiableAssertion", "EqualsWithItself"})
public class FindingTest {

  private static SourcePositionBuilder spb() {
    return new SourcePositionBuilder();
  }

  @Test
  public void testShouldEqualItself1() {
    // Given
    Finding f = new Finding(Finding.Type.ERROR, "A");

    // When && Then
    assertTrue(f.equals(f));
  }

  @Test
  public void testShouldEqualItself2() {
    // Given
    SourcePosition sp = spb().setLine(0).setColumn(1).setFileName("F").build();
    Finding f = new Finding(Finding.Type.ERROR, "E", sp);

    // When && Then
    assertTrue(f.equals(f));
  }

  @Test
  public void testShouldEqualItself3() {
    // Given
    SourcePosition sp = spb().setLine(0).setColumn(1).setFileName("F").build();
    Finding f = new Finding(Finding.Type.WARNING, "E", sp);

    // When && Then
    assertTrue(f.equals(f));
  }

  @Test
  public void testShouldEqualItself4() {
    // Given
    SourcePosition sp = spb().setLine(0).setColumn(1).setFileName("F").build();
    Finding f = new Finding(Finding.Type.USER_ERROR, "E", sp);

    // When && Then
    assertTrue(f.equals(f));
  }

  @Test
  public void testShouldEqualItself5() {
    // Given
    SourcePosition sp = spb().setLine(0).setColumn(1).setFileName("F").build();
    SourcePosition spe = spb().setLine(2).setColumn(3).setFileName("F").build();
    Finding f = new Finding(Finding.Type.ERROR, "E", sp, spe);

    // When && Then
    assertTrue(f.equals(f));
  }

  @Test
  public void testShouldEqual1() {
    // Given
    Finding f1 = new Finding(Finding.Type.ERROR, "A");
    Finding f2 = new Finding(Finding.Type.ERROR, "A");

    // When && Then
    assertTrue(f1.equals(f2));
  }

  @Test
  public void testShouldEqual2() {
    // Given
    Finding f1 = new Finding(Finding.Type.WARNING, "A");
    Finding f2 = new Finding(Finding.Type.WARNING, "A");

    // When && Then
    assertTrue(f1.equals(f2));
  }

  @Test
  public void testShouldEqual3() {
    // Given
    Finding f1 = new Finding(Finding.Type.USER_ERROR, "A");
    Finding f2 = new Finding(Finding.Type.USER_ERROR, "A");

    // When && Then
    assertTrue(f1.equals(f2));
  }

  @Test
  public void testShouldEqual4() {
    // Given
    SourcePosition sp1 = spb().setLine(0).setColumn(1).setFileName("F").build();
    SourcePosition sp2 = spb().setLine(0).setColumn(1).setFileName("F").build();
    Finding f1 = new Finding(Finding.Type.ERROR, "E", sp1);
    Finding f2 = new Finding(Finding.Type.ERROR, "E", sp2);

    // When && Then
    assertTrue(f1.equals(f2));
  }

  @Test
  public void testShouldEqual5() {
    // Given
    SourcePosition sp1 = spb().setLine(0).setColumn(1).setFileName("F").build();
    SourcePosition spe1 = spb().setLine(2).setColumn(3).setFileName("F").build();
    SourcePosition sp2 = spb().setLine(0).setColumn(1).setFileName("F").build();
    SourcePosition spe2 = spb().setLine(2).setColumn(3).setFileName("F").build();
    Finding f1 = new Finding(Finding.Type.ERROR, "E", sp1, spe1);
    Finding f2 = new Finding(Finding.Type.ERROR, "E", sp2, spe2);

    // When && Then
    assertTrue(f1.equals(f2));
  }

  @Test
  public void testShouldNotEqual1() {
    // Given
    Finding f1 = new Finding(Finding.Type.ERROR, "A");
    Finding f2 = new Finding(Finding.Type.ERROR, "B");

    // When && Then
    assertFalse(f1.equals(f2));
  }

  @Test
  public void testShouldNotEqual2() {
    // Given
    Finding f1 = new Finding(Finding.Type.ERROR, "A");
    Finding f2 = new Finding(Finding.Type.WARNING, "A");

    // When && Then
    assertFalse(f1.equals(f2));
  }

  @Test
  public void testShouldNotEqual3() {
    // Given
    Finding f1 = new Finding(Finding.Type.ERROR, "A");
    Finding f2 = new Finding(Finding.Type.WARNING, "B");

    // When && Then
    assertFalse(f1.equals(f2));
  }

  @Test
  public void testShouldNotEqual4() {
    // Given
    SourcePosition sp1 = spb().setLine(0).setColumn(1).setFileName("F").build();
    SourcePosition sp2 = spb().setLine(0).setColumn(1).setFileName("!F").build();
    Finding f1 = new Finding(Finding.Type.ERROR, "E", sp1);
    Finding f2 = new Finding(Finding.Type.ERROR, "E", sp2);

    // When && Then
    assertFalse(f1.equals(f2));
  }

  @Test
  public void testShouldNotEqual5() {
    // Given
    SourcePosition sp1 = spb().setLine(0).setColumn(1).setFileName("F").build();
    SourcePosition sp2 = spb().setLine(2).setColumn(1).setFileName("F").build();
    Finding f1 = new Finding(Finding.Type.ERROR, "E", sp1);
    Finding f2 = new Finding(Finding.Type.ERROR, "E", sp2);

    // When && Then
    assertFalse(f1.equals(f2));
  }

  @Test
  public void testShouldNotEqual6() {
    // Given
    SourcePosition sp1 = spb().setLine(0).setColumn(1).setFileName("F").build();
    SourcePosition sp2 = spb().setLine(0).setColumn(2).setFileName("F").build();
    Finding f1 = new Finding(Finding.Type.ERROR, "E", sp1);
    Finding f2 = new Finding(Finding.Type.ERROR, "E", sp2);

    // When && Then
    assertFalse(f1.equals(f2));
  }

  @Test
  public void testShouldEqual7() {
    // Given
    SourcePosition sp1 = spb().setLine(0).setColumn(1).setFileName("F").build();
    SourcePosition spe1 = spb().setLine(2).setColumn(3).setFileName("F").build();
    SourcePosition sp2 = spb().setLine(0).setColumn(1).setFileName("F").build();
    SourcePosition spe2 = spb().setLine(4).setColumn(3).setFileName("F").build();
    Finding f1 = new Finding(Finding.Type.ERROR, "E", sp1, spe1);
    Finding f2 = new Finding(Finding.Type.ERROR, "E", sp2, spe2);

    // When && Then
    assertFalse(f1.equals(f2));
  }

  @Test
  public void testShouldEqual8() {
    // Given
    SourcePosition sp1 = spb().setLine(0).setColumn(1).setFileName("F").build();
    SourcePosition spe1 = spb().setLine(2).setColumn(3).setFileName("F").build();
    SourcePosition sp2 = spb().setLine(0).setColumn(1).setFileName("F").build();
    SourcePosition spe2 = spb().setLine(2).setColumn(5).setFileName("F").build();
    Finding f1 = new Finding(Finding.Type.ERROR, "E", sp1, spe1);
    Finding f2 = new Finding(Finding.Type.ERROR, "E", sp2, spe2);

    // When && Then
    assertFalse(f1.equals(f2));
  }
}
