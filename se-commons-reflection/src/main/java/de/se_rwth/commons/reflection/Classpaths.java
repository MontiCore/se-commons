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
package de.se_rwth.commons.reflection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.se_rwth.commons.StringMatchers;

/**
 * Static helper functions to work with the Java classpath.
 *
 * @author (last commit) $Author: antonio $
 * @version $Revision: 13097 $, $Date: 2012-08-16 06:56:24 +0200 (Do, 16 Aug
 * 2012) $
 */
public final class Classpaths {

  /**
   * @return true, if a class of the given name can be found using the system
   * class loader.
   */
  public static boolean classExists(String className) {
    return classExists(className, ClassLoader.getSystemClassLoader());
  }

  /**
   * @return true, if a class of the given name can be found using the given
   * class loader.
   */
  public static boolean classExists(String className, ClassLoader classLoader) {
    boolean exists = true;
    try {
      Class.forName(className, false, classLoader);
    }
    catch (ClassNotFoundException e) {
      exists = false;
    }
    return exists;
  }

  /**
   * Tries to find out if the loading of a class of the given class name failed
   * because of spelling mistakes.
   */
  public static final <T> Collection<String> guessIntendedClassName(final String className,
      Class<T> superType) {

    checkNotNull(className);
    checkNotNull(superType);

    final Collection<String> suggestions = newArrayList();

    Set<Class<?>> typeImplementations = classpathIndices.get(superType);

    /* Scan the classpath for implementations of the given type if no cached
     * result of previous scans exists. */
    if (typeImplementations == null) {

      Reflections reflections =
          new Reflections(
              new ConfigurationBuilder()
                  .setUrls(ClasspathHelper.forClassLoader(Classpaths.class.getClassLoader()))
                  .setScanners(new SubTypesScanner()));

      Set<Class<? extends T>> classes = reflections.getSubTypesOf(superType);
      typeImplementations = ImmutableSet.<Class<?>> copyOf(classes);

      classpathIndices.put(superType, typeImplementations);

    }

    /* Collect suggestions. We select a class as a candidate for the actually
     * intended class name if the levenshtein distance is less then 5. */
    for (Class<?> typeImplementation : typeImplementations) {
      String implementationName = typeImplementation.getName();
      int distance = StringMatchers.getLevenshteinDistance(implementationName, className);

      if (distance < 5) {
        suggestions.add(implementationName);
      }
    }

    return ImmutableList.copyOf(suggestions);
  }

  /**
   * Tries to find out if the loading of a resource of the given name failed
   * because of spelling mistakes.
   */
  public static final Collection<String> guessIntendedResourceName(final String resourceName) {

    checkNotNull(resourceName);

    Reflections reflections =
        new Reflections(
            new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forClassLoader(Classpaths.class.getClassLoader()))
                .setScanners(new SubTypesScanner()));

    Set<String> suggestions = reflections.getResources(name ->
        name != null ? StringMatchers.getLevenshteinDistance(resourceName, name) < 5 : false);

    return ImmutableList.copyOf(suggestions);
  }

  /**
   * Cache of all relevant classes and resources on the classpath.
   */
  private static final Map<Class<?>, Set<Class<?>>> classpathIndices = newHashMap();

  /**
   * Private constructor permitting manual instantiation.
   */
  private Classpaths() {
  }

}
