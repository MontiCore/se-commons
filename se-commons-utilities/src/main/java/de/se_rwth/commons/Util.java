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

import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class Util {

  private Util() {
    // noninstantiable
  }

  /**
   * Creates a list by sequentially applying a UnaryOperator until the UnaryOperator returns null.
   * <p>
   * If the elements generated by the getNext operator already exist, this function could be said to
   * "discover" a linked list structure in memory.
   *
   * @param first   the element with which list generation should start
   * @param getNext a function generating the subsequent element to each element
   * @return a list of all the generated elements, in the order in which they were generated,
   * including the first element and the last element that wasn't null.
   * @see #listTillPredicate(Object, UnaryOperator, Predicate)
   */
  public static <T> List<T> listTillNull(T first, UnaryOperator<T> getNext) {
    return listTillPredicate(first, getNext, next -> next != null);
  }

  /**
   * Creates a list by sequentially applying a UnaryOperator until a given predicate fails.
   * <p>
   * If the elements generated by the getNext operator already exist, this function could be said to
   * "discover" a linked list structure in memory.
   *
   * @param first     the element with which list generation should start
   * @param getNext   a function generating the subsequent element to each element
   * @param predicate a halting condition, preventing infinite iteration
   * @return a list of all the generated elements, in the order in which they were generated,
   * including the first element and the last element that matched the predicate
   */
  public static <T> List<T> listTillPredicate(T first, UnaryOperator<T> getNext,
      Predicate<T> predicate) {
    List<T> result = new ArrayList<>();
    for (T next = first; predicate.test(next); next = getNext.apply(next)) {
      result.add(next);
    }
    return result;
  }

  /**
   * Creates a List that flattens the subtree spanned by the root in counterclockwise pre-order
   * fashion.
   *
   * @param root               the root of the subtree to be iterated over
   * @param generatingFunction a function that navigates from an arbitrary node to its children
   * @return a List of the subtree spanned by the root, ordered in pre-order
   */
  public static <T> List<T> preOrder(T root, Function<T, Collection<T>> generatingFunction) {
    TreeTraverser treeTraverser = new TreeTraverser<T>() {

      @Override public Iterable<T> children(T root) {
        return generatingFunction.apply(root);
      }
    };
    return Lists.newArrayList(treeTraverser.preOrderTraversal(root));
  }
}
