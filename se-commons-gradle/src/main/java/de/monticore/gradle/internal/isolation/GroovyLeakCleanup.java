/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal.isolation;

import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.beans.Introspector;

@SuppressWarnings("unused")
public class GroovyLeakCleanup   {
  /**
   * Ok, this is really ugly:
   * Because the GroovyInterpreter does not clean up behind itself,
   * we have to manually clear the {@link ClassInfo} and {@link ClassValue} caches
   */
  public static void cleanUp() {
    for (ClassInfo ci : ClassInfo.getAllClassInfo()) {
      InvokerHelper.removeClass(ci.getTheClass());
    }
    Introspector.flushCaches();
  }
}
