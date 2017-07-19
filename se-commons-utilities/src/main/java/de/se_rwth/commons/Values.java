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

import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

/**
 * Helper functions for working with values.
 *
 */
public final class Values {

  /**
   * @return the given object if it is of the given type or an absent Optional
   * otherwise.
   */
  @SuppressWarnings("unchecked")
  public static <T> Optional<T> checkType(Object object, Class<T> type) {
    return object == null
        ? Optional.empty()
        : type.isAssignableFrom(object.getClass())
            ? Optional.of((T) object)
            : Optional.empty();
  }

  /**
   * @return the given object if it is of a list containing elements of the
   * given type or an absent Optional otherwise.
   */
  @SuppressWarnings("unchecked")
  public static <T> Optional<List<T>> checkTypes(Object object, Class<T> type) {
    return object == null
        ? Optional.empty()
        : List.class.isAssignableFrom(object.getClass())
            ? Optional.of((List<T>) object)
            : checkType(object, type).isPresent()
                ? Optional.of(ImmutableList.of((T) object))
                : Optional.empty();
  }

  /**
   * Private constructor permitting manual instantiation.
   */
  private Values() {
  }

}
