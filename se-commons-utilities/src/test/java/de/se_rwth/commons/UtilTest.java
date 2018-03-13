/* (c) https://github.com/MontiCore/monticore */

package de.se_rwth.commons;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * @author Sebastian Oberhoff
 */
public final class UtilTest {

  private static class TestNode {

    private String content;

    private TestNode parent;

    private Collection<TestNode> children = new ArrayList<>();

    private TestNode(String content) {
      this.content = content;
    }

  }

  TestNode A;

  TestNode B;

  TestNode C;

  TestNode D;

  TestNode E;

  /**
   * Creates a tree of the shape
   *
   * A
   * | \
   * B  E
   * |\
   * C D
   *
   * with the alphabetic order corresponding to a preorder traversal.
   */
  @Before
  public void setupTree() {
    A = new TestNode("A");
    B = new TestNode("B");
    C = new TestNode("C");
    D = new TestNode("D");
    E = new TestNode("E");

    A.children.add(B);
    A.children.add(E);
    B.children.add(C);
    B.children.add(D);

    B.parent = A;
    C.parent = B;
    D.parent = B;
    E.parent = A;
  }

  @Test
  public void testPreorder() {
    String preorder = Util.preOrder(A, testNode -> testNode.children).stream()
        .map(testNode -> testNode.content)
        .collect(Collectors.joining());
    assertEquals("ABCDE", preorder);
  }

  @Test
  public void testListTillPredicate() {
    String listTillPredicate = Util.listTillPredicate(D, testNode -> testNode.parent,
        testNode -> !testNode.content.equals("A")).stream()
        .map(testNode -> testNode.content)
        .collect(Collectors.joining());
    assertEquals("DB", listTillPredicate);
  }

  @Test
  public void testListTillNull(){
    String listTillPredicate = Util.listTillNull(C, testNode -> testNode.parent).stream()
        .map(testNode -> testNode.content)
        .collect(Collectors.joining());
    assertEquals("CBA", listTillPredicate);
  }
}
