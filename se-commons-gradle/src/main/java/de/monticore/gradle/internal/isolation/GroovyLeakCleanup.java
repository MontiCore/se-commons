/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.isolation;

import de.monticore.gradle.internal.isolation.ref.UnsafeFieldUtil;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClassRegistry;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.util.Iterator;

@SuppressWarnings("unused")
public class GroovyLeakCleanup   {
  /**
   * Ok, this is really ugly:
   * Because the GroovyInterpreter does not clean up behind itself,
   * we have to manually clear the {@link ClassInfo} and {@link ClassValue} caches.
   *
   * And it gets even worse: Due to a {@link java.lang.invoke.MethodHandle}
   * memory leak up to java 17, we also have to attempt to clean up
   * cached method handles
   */
  public static void cleanUp() {
    for (ClassInfo ci : ClassInfo.getAllClassInfo()) {
      InvokerHelper.removeClass(ci.getTheClass());
    }
    Introspector.flushCaches();
    MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
    Iterator<?> it = registry.iterator();
    while (it.hasNext())
      it.remove();
    Runtime.getRuntime().gc();

    // MethodHandle leaks classes and thus prevents the garbage collector
    // from working on groovy classloaders
    // https://bugs.openjdk.org/browse/JDK-8078641
    // This bug is fixed in java 18+
    try {
      Field arraysField = Class.forName("java.lang.invoke.MethodHandleImpl").getDeclaredField("ARRAYS");
      UnsafeFieldUtil.setFinalStatic(arraysField, null);
    } catch (Exception ignored) {
      // we can only attempt to clean up MethodHandleImpl#ARRAYS
      // depending on the runtime, it may not be possible
    }
  }

}
