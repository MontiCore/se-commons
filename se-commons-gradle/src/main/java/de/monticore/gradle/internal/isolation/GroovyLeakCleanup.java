/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.isolation;

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
   */
  public static void cleanUp() {
    for (ClassInfo ci : ClassInfo.getAllClassInfo()) {
      InvokerHelper.removeClass(ci.getTheClass());
    }
    Introspector.flushCaches();
    MetaClassRegistry registry = GroovySystem.getMetaClassRegistry();
    Iterator<?> it = registry.iterator();
    while (it.hasNext()) {
      it.remove();
    }
  }

}
