/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.internal;

import de.monticore.gradle.queue.CachedQueueService;
import de.monticore.gradle.queue.ICachedQueueService;
import org.gradle.api.file.FileCollection;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.workers.WorkerExecutor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Forwards calls of an {@link ICachedQueueService} proxy to a service instance whose class was
 * loaded by a different classloader (i.e., the {@link CachedQueueService} was registered by
 * another copy of this plugin, e.g., one applied to a different subproject).
 * <p>
 * Only Gradle API types (shared between all plugin classloaders) and primitives may be used
 * within the signatures of the {@link ICachedQueueService}.
 */
public class CrossClassLoaderServiceHandler implements InvocationHandler {

  protected final Object target;

  protected final Map<Method, Method> methodCache = new ConcurrentHashMap<>();

  public CrossClassLoaderServiceHandler(Object target) {
    this.target = Objects.requireNonNull(target);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    switch (method.getName()) {
      case "equals":
        if (method.getParameterCount() == 1) {
          return proxy == args[0];
        }
        break;
      case "hashCode":
        if (method.getParameterCount() == 0) {
          return System.identityHashCode(proxy);
        }
        break;
      case "toString":
        if (method.getParameterCount() == 0) {
          return "ICachedQueueService proxy for " + target;
        }
        break;
      default:
        break;
    }

    try {
      Method targetMethod = methodCache.computeIfAbsent(method, this::findTargetMethod);
      if (targetMethod != null) {
        return targetMethod.invoke(target, args);
      }
      if (isNewWorkQueueMethodWithServiceRegistry(method)) {
        // The other classloader contains an older version of this library, which does not
        // yet support the lazy initialization => fall back to the old variant
        // noinspection JavaReflectionInvocation - old API
        return target.getClass()
            .getMethod("newWorkQueue", WorkerExecutor.class, FileCollection.class)
            .invoke(target, args[0], args[2]);
      }
    } catch (InvocationTargetException e) {
      throw e.getCause() != null ? e.getCause() : e;
    }
    throw new UnsupportedOperationException("The CachedQueueService (" + target.getClass().getName()
        + ") was registered by a different (incompatible) version of se-commons-gradle and does not support "
        + method + ". Ensure that all projects use the same version of the plugins "
        + "(e.g., by adding them to the root project, possibly with apply false).");
  }

  protected Method findTargetMethod(Method method) {
    try {
      Method m = target.getClass().getMethod(method.getName(), method.getParameterTypes());
      m.setAccessible(true); // The (generated) service class might not be public
      return m;
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  protected static boolean isNewWorkQueueMethodWithServiceRegistry(Method method) {
    Class<?>[] p = method.getParameterTypes();
    return method.getName().equals("newWorkQueue") && p.length == 3
        && p[0] == WorkerExecutor.class && p[1] == ServiceRegistry.class
        && p[2] == FileCollection.class;
  }
}
