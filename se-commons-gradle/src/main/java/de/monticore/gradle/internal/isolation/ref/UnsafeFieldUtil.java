/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.isolation.ref;

import jdk.internal.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @deprecated do not use
 */
@Deprecated
public class UnsafeFieldUtil {

  private static Unsafe unsafe;

  static {
    try {
      final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      unsafe = (Unsafe) unsafeField.get(null);
    } catch (ReflectiveOperationException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static void setFinalStatic(Field field, Object value) {
    Object fieldBase = unsafe.staticFieldBase(field);
    long fieldOffset = unsafe.staticFieldOffset(field);

    unsafe.putObject(fieldBase, fieldOffset, value);
  }
}