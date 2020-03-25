/* (c) https://github.com/MontiCore/monticore */

package de.se_rwth.commons;

import java.util.function.Function;

import com.google.common.collect.TreeTraverser;

/**
 * Wrapper for Guava's {@link TreeTraverser} turning common operations into one-liners.
 * 
 */
@Deprecated
// "use the Util class instead"
public final class TreeUtil {
  
  private TreeUtil() {
    // noninstantiable
  }
  
  /**
   * Google Guava's TreeTraverser unfortunately requires instantiation via at least an anonymous
   * inner class, since the children() method is abstract. This method allows the instantiation with
   * just a lambda expression instead.
   * <p>
   * Example: {@code createTreeTraverser(node -> node.children)}
   * 
   * @param getChildren a function that navigates from a given node to its children
   * @return a TreeTraverser for traversing the tree spanned by the getChildren function
   */
  public static <T> TreeTraverser<T> createTreeTraverser(final Function<T, Iterable<T>> getChildren) {
    return new TreeTraverser<T>() {
      
      @Override
      public Iterable<T> children(T root) {
        return getChildren.apply(root);
      }
    };
  }
  
  /**
   * Creates an Iterable that flattens the subtree spanned by the root in pre-order fashion.
   * 
   * @param root the root of the subtree to be iterated over
   * @param getChildren a function that navigates from an arbitrary node to its children
   * @return an Iterable over the subtree spanned by the root, ordered in pre-order
   */
  public static <T> Iterable<T> preOrder(T root, Function<T, Iterable<T>> getChildren) {
    return createTreeTraverser(getChildren).preOrderTraversal(root);
  }
  
  /**
   * Creates an Iterable that flattens the subtree spanned by the root in post-order fashion.
   * 
   * @param root the root of the subtree to be iterated over
   * @param getChildren a function that navigates from an arbitrary node to its children
   * @return an Iterable over the subtree spanned by the root, ordered in post-order
   */
  public static <T> Iterable<T> postOrder(T root, Function<T, Iterable<T>> getChildren) {
    return createTreeTraverser(getChildren).postOrderTraversal(root);
  }
  
  /**
   * Creates an Iterable that flattens the subtree spanned by the root in breadth-first fashion.
   * 
   * @param root the root of the subtree to be iterated over
   * @param getChildren a function that navigates from an arbitrary node to its children
   * @return an Iterable over the subtree spanned by the root, ordered breadth-first
   */
  public static <T> Iterable<T> breadthFirst(T root, Function<T, Iterable<T>> getChildren) {
    return createTreeTraverser(getChildren).breadthFirstTraversal(root);
  }
}
