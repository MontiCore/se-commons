/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.isolation;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

//TODO: Remove once we switch to Java 18 (or higher)
@SuppressWarnings("unused")
public class JDK8078641Cleanup {

  /**
   * And it gets even more worse:
   * <a href="https://bugs.openjdk.org/browse/JDK-8078641">JDK-8078641</a> in
   * combination with child classloaders retains the MethodHandles within the child classloader
   * <p>
   * Solution: unset all MethodHandleImpl.asTypeCache
   */
  public static void cleanUp() throws ReflectiveOperationException {
    Class<?> methodHandleImplClass = Class.forName("java.lang.invoke.MethodHandleImpl");
    Class<?> methodHandleStaticsClass = Class.forName("java.lang.invoke.MethodHandleStatics");

    Method varargsArrayMethod = methodHandleImplClass.getDeclaredMethod("varargsArray", int.class);
    varargsArrayMethod.setAccessible(true);

    // The ARRAYS (accessed via varargsArray) has a length of MAX_ARITY
    Field maxArityField = methodHandleStaticsClass.getDeclaredField("MAX_ARITY");
    maxArityField.setAccessible(true);
    int max_arity = maxArityField.getInt(null);

    Field asTypeCacheField = MethodHandle.class.getDeclaredField("asTypeCache");
    asTypeCacheField.setAccessible(true);

    // Now that we have all fields/methods/values
    for (int i = 0; i < max_arity; i++) {
      // We can set the asTypeCache to null for every element
      MethodHandle mh = (MethodHandle) varargsArrayMethod.invoke(null, i);
      asTypeCacheField.set(mh, null);
    }

  }

}
