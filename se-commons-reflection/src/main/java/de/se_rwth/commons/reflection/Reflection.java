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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * Provides static final convenience methods to work with reflection and
 * annotations. Reflection is evil, use it with caution and only if necessary.
 * Favor the methods that access only public methods/fields over methods that
 * access declared/all fields.
 *
 */
public class Reflection {

  /**
   * Returns all declared fields of the given object annotated with the given
   * annotation based on the given class.
   */
  public static final Collection<Field> getAnnotatedDeclaredFields(Class<?> clazz,
      Class<? extends Annotation> annotation) {

    assert clazz != null;
    assert annotation != null;

    Collection<Field> annotatedFields = newArrayList();

    for (Field field : clazz.getDeclaredFields()) {
      if (isAnnotated(field, annotation)) {
        field.setAccessible(true);
        annotatedFields.add(field);
      }
    }

    return annotatedFields;
  }

  /**
   * Returns all declared fields of the given object annotated with the given
   * annotation based on the given class.
   */
  public static final Collection<Field> getAnnotatedDeclaredFields(Object object,
      Class<? extends Annotation> annotation) {
    return getAnnotatedDeclaredFields(object.getClass(), annotation);
  }

  /**
   * Returns all declared methods of the given object annotated with the given
   * annotation based on the given class.
   */
  public static final Collection<Method> getAnnotatedDeclaredMethods(Class<?> clazz,
      Class<? extends Annotation> annotation) {

    assert clazz != null;
    assert annotation != null;

    Collection<Method> annotatedMethods = newArrayList();

    for (Method method : clazz.getMethods()) {
      if (isAnnotated(method, annotation)) {
        method.setAccessible(true);
        annotatedMethods.add(method);
      }
    }

    return annotatedMethods;

  }

  /**
   * Returns all declared methods of the given object annotated with the given
   * annotation.
   */
  public static final Collection<Method> getAnnotatedDeclaredMethods(Object object,
      Class<? extends Annotation> annotation) {
    return getAnnotatedDeclaredMethods(object.getClass(), annotation);
  }

  /**
   * Returns all fields of the given class annotated with the given annotation.
   */
  public static final Collection<Field> getAnnotatedFields(final Class<?> clazz,
      Class<? extends Annotation> annotation) {

    checkNotNull(clazz);
    checkNotNull(annotation);
    
    ImmutableSet.Builder<Field> annotatedFields = ImmutableSet.builder();

    Class<?> c = clazz;

    while (c != null) {
      annotatedFields.addAll(getAnnotatedDeclaredFields(c, annotation));
      c = c.getSuperclass();
    }
    
    return annotatedFields.build();
  }

  /**
   * Returns all fields of the given object annotated with the given annotation.
   */
  public static final Collection<Field> getAnnotatedFields(Object object,
      Class<? extends Annotation> annotation) {
    return getAnnotatedFields(object.getClass(), annotation);
  }

  /**
   * Returns all methods of the given object annotated with the given
   * annotation.
   */
  public static final Collection<Method> getAnnotatedMethods(Class<?> clazz,
      Class<? extends Annotation> annotation) {

    checkNotNull(clazz);
    checkNotNull(annotation);

    ImmutableSet.Builder<Method> annotatedMethods = ImmutableSet.builder();

    Class<?> c = clazz;

    while (c != null) {
      annotatedMethods.addAll(getAnnotatedDeclaredMethods(c, annotation));
      c = c.getSuperclass();
    }

    return annotatedMethods.build();
  }

  /**
   * Returns all methods of the given class annotated with the given annotation.
   */
  public static final Collection<Method> getAnnotatedMethods(Object object,
      Class<? extends Annotation> annotation) {
    return getAnnotatedMethods(object.getClass(), annotation);
  }

  /**
   * Returns all public and inherited fields of the given class annotated with
   * the given annotation.
   */
  public static final Collection<Field> getAnnotatedPublicFields(Class<?> clazz,
      Class<? extends Annotation> annotation) {

    assert clazz != null;
    assert annotation != null;

    Collection<Field> annotatedFields = newArrayList();

    for (Field field : clazz.getFields()) {
      if (isAnnotated(field, annotation)) {
        field.setAccessible(true);
        annotatedFields.add(field);
      }
    }

    return annotatedFields;
  }

  /**
   * Returns all public and inherited fields of the given object annotated with
   * the given annotation.
   */
  public static final Collection<Field> getAnnotatedPublicFields(Object object,
      Class<? extends Annotation> annotation) {
    return getAnnotatedPublicFields(object.getClass(), annotation);
  }

  /**
   * Returns all public and inherited methods of the given object annotated with
   * the given annotation based on the given class.
   */
  public static final Collection<Method> getAnnotatedPublicMethods(Class<?> clazz,
      Class<? extends Annotation> annotation) {

    assert clazz != null;
    assert annotation != null;

    Collection<Method> annotatedMethods = newArrayList();

    for (Method method : clazz.getMethods()) {
      if (isAnnotated(method, annotation)) {
        method.setAccessible(true);
        annotatedMethods.add(method);
      }
    }

    return annotatedMethods;
  }

  /**
   * Returns all public and inherited methods of the given object annotated with
   * the given annotation based on the given class.
   */
  public static final Collection<Method> getAnnotatedPublicMethods(Object object,
      Class<? extends Annotation> annotation) {
    return getAnnotatedPublicMethods(object.getClass(), annotation);
  }

  /**
   * Returns the value of the given field of the given object.
   * Reflection-related exceptions are silently dismissed. In such cases, an
   * absent {@link Optional} is returned.
   */
  public static final <T> Optional<T> getFieldValue(Object object, Field field) {
    
    assert object != null;
    assert field != null;

    T value = null;

    try {
      field.setAccessible(true);
      @SuppressWarnings("unchecked")
      T casted = (T) field.get(object);
      value = casted;
    }
    catch (IllegalArgumentException e) {
      logger.warn("Could not get value of field " + field + " in " + object, e);
    }
    catch (IllegalAccessException e) {
      logger.warn("Could not get value of field " + field + " in " + object, e);
    }
    catch (ClassCastException e) {
      logger.warn("Could not get value of field " + field + " in " + object, e);
    }

    return Optional.ofNullable(value);
  }

  /**
   * Returns the first generic parameter of the super class of a given class or
   * null if the super class has no generic parameters.
   */
  public static final Class<?> getSuperclassTypeParameter(Class<?> subClass) {

    assert subClass != null;

    Class<?> typeParameter = null;

    Type superClass = subClass.getGenericSuperclass();

    try {
      ParameterizedType parameterizedType = (ParameterizedType) superClass;
      Type[] typeParameters = parameterizedType.getActualTypeArguments();
      if (typeParameters.length > 0) {
        typeParameter = getRawType(typeParameters[0]);
      }
    }
    catch (ClassCastException e) {
      /* Silently ignore this exception. A failed cast means that superClass has
       * no type parameter. We then return null. */
    }

    return typeParameter;
  }

  /**
   * Injects the given object into a field of the given object annotated with
   * the given annotation.
   */
  public static final void injectField(Object fieldValue, Object object,
      Class<? extends Annotation> fieldAnnotation) {

    Collection<Field> annotatedFields = getAnnotatedPublicFields(object, fieldAnnotation);
    Class<?> fieldValueClass = fieldValue.getClass();

    for (Field field : annotatedFields) {
      if (field.getType().isAssignableFrom(fieldValueClass)) {
        try {
          field.setAccessible(true);
          field.set(object, fieldValue);
        }
        catch (IllegalArgumentException e) {
          throw new IllegalStateException("Failed to inject into field.", e);
        }
        catch (IllegalAccessException e) {
          throw new IllegalStateException("Failed to inject into field.", e);
        }
      }
      else {
        throw new IllegalStateException("Failed to inject into field. Types are not compatible.");
      }
    }

  }

  /**
   * Invokes the given method on the given object and wraps all possible
   * exception in a generic {@link ReflectionException}.
   */
  public static final <T> Optional<T> invoke(Object object, Method method, Object... parameters)
      throws ReflectionException {

    T result = null;

    try {
      @SuppressWarnings("unchecked")
      T casted = (T) method.invoke(object, parameters);
      result = casted;
    }
    catch (IllegalArgumentException e) {
      throw ReflectionException.of("Could not execute method " + method + " in " + object, e);
    }
    catch (IllegalAccessException e) {
      throw ReflectionException.of("Could not execute method " + method + " in " + object, e);
    }
    catch (InvocationTargetException e) {
      throw ReflectionException.of("Could not execute method " + method + " in " + object, e);
    }
    catch (ClassCastException e) {
      throw ReflectionException.of("Could not execute method " + method + " in " + object, e);
    }

    return Optional.ofNullable(result);
  }

  /**
   * Invokes all public and inherited methods of the given object annotated with
   * the given annotation based on the given class with the given arguments.
   */
  public static final void invokeAnnotatedMethods(Object object,
      Class<? extends Annotation> annotation,
      Object... parameters) throws ReflectionException {

    assert object != null;
    assert annotation != null;

    Method[] methods = object.getClass().getMethods();
    for (Method method : methods) {
      if (method.getAnnotation(annotation) != null) {
        if (method.getParameterTypes().length == parameters.length) {
          invoke(object, method, parameters);
        }
      }
    }

  }

  /**
   * Invokes the given method on the given object silently, that means, not
   * propagating any exceptions.
   */
  public static final <T> Optional<T> invokeSilently(Object object, Method method, Object... parameters) {

    Optional<T> result = null;

    try {
      @SuppressWarnings("unchecked")
      Optional<T> casted = (Optional<T>) invoke(object, method, parameters);
      result = casted;
    }
    catch (ReflectionException e) {
      logger.debug("Could not execute method " + method + " in " + object, e);
    }

    return result;
  }

  /**
   * Invokes the given static method of the given class and wraps all possible
   * exception in a generic {@link ReflectionException}.
   */
  public static final <T> Optional<T> invokeStatic(Method method, Object... parameters)
      throws ReflectionException {
    return invoke(null, method, parameters);
  }

  /**
   * Returns true if the given field is annotated with the given annotation.
   */
  public static final boolean isAnnotated(Field field, Class<? extends Annotation> annotation) {
    return field.getAnnotation(annotation) != null;
  }

  /**
   * Returns true if the given method is annotated with the given annotation.
   */
  public static final boolean isAnnotated(Method method, Class<? extends Annotation> annotation) {
    return method.getAnnotation(annotation) != null;
  }

  /**
   * Returns the raw type of the given type.
   */
  private static final Class<?> getRawType(Type type) {

    assert type != null;

    Class<?> rawType = null;

    if (type instanceof Class<?>) {
      rawType = (Class<?>) type;
    }
    else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type concreteRawType = parameterizedType.getRawType();
      checkArgument(concreteRawType instanceof Class);
      rawType = (Class<?>) concreteRawType;
    }
    else if (type instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      rawType = Array.newInstance(getRawType(componentType), 0).getClass();
    }
    else if (type instanceof TypeVariable) {
      rawType = Object.class;
    }
    else {
      throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
          + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
    }

    return rawType;
  }

  /**
   * SLF4J logger
   */
  private static final Logger logger = LoggerFactory.getLogger(Reflection.class);

  /**
   * Private constructor for class with static utilities.
   */
  private Reflection() {
  }

}
