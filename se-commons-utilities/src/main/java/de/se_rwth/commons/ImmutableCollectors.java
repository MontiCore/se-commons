/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Provides {@link Collectors} for immutable collections.
 *
 */
public class ImmutableCollectors {
  
  /**
   * @return a {@link Collector} for {@link ImmutableLists}s.
   */
  public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {
    Supplier<ImmutableList.Builder<T>> supplier = ImmutableList.Builder::new;
    BiConsumer<ImmutableList.Builder<T>, T> accumulator = (list, element) -> list.add(element);
    BinaryOperator<ImmutableList.Builder<T>> combiner = (list, otherSet) -> list.addAll(otherSet.build());
    Function<ImmutableList.Builder<T>, ImmutableList<T>> finisher = ImmutableList.Builder::build;
    return Collector.of(supplier, accumulator, combiner, finisher);
  }

  /**
   * @return a {@link Collector} for {@link ImmutableMap}s.
   */
  public static <T, K, V> Collector<T, ?, ImmutableMap<K, V>> toImmutableMap(
      Function<? super T, ? extends K> keyMapper,
      Function<? super T, ? extends V> valueMapper) {
    Supplier<ImmutableMap.Builder<K, V>> supplier = ImmutableMap.Builder::new;
    BiConsumer<ImmutableMap.Builder<K, V>, T> accumulator = (map, element)
        -> map.put(keyMapper.apply(element), valueMapper.apply(element));
    BinaryOperator<ImmutableMap.Builder<K, V>> combiner = (map, otherMap) -> map.putAll(otherMap.build());
    Function<ImmutableMap.Builder<K, V>, ImmutableMap<K, V>> finisher = ImmutableMap.Builder::build;
    return Collector.of(supplier, accumulator, combiner, finisher);
  }
  
  /**
   * @return a {@link Collector} for {@link ImmutableSet}s.
   */
  public static <T> Collector<T, ?, ImmutableSet<T>> toImmutableSet() {
    Supplier<ImmutableSet.Builder<T>> supplier = ImmutableSet.Builder::new;
    BiConsumer<ImmutableSet.Builder<T>, T> accumulator = (b, v) -> b.add(v);
    BinaryOperator<ImmutableSet.Builder<T>> combiner = (l, r) -> l.addAll(r.build());
    Function<ImmutableSet.Builder<T>, ImmutableSet<T>> finisher = ImmutableSet.Builder::build;
    return Collector.of(supplier, accumulator, combiner, finisher);
  }

  /**
   * Private constructor permitting manual instantiation.
   */
  private ImmutableCollectors() {
  }
  
}
