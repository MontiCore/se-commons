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

package de.se_rwth.commons;

import java.util.function.Function;

import com.google.common.collect.TreeTraverser;

/**
 * Wrapper for Guava's {@link TreeTraverser} turning common operations into one-liners.
 * 
 * @author Sebastian Oberhoff
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
