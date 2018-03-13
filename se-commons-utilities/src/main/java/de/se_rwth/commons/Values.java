/* (c) https://github.com/MontiCore/monticore */
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
