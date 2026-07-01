/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.common;

import de.se_rwth.commons.logging.Log;
import java.util.Arrays;

/**
 * Action to represent the invocation of a tool.
 * Uses the mainClass property to call (mainClass)#gradleMain.
 * This avoids defining your own ToolInvoker
 */
public abstract class SharedToolAction extends AToolAction {

  protected void doRun(String[] args) {
    if (!getParameters().getMainClass().isPresent())
      throw new IllegalStateException("MainClass property is required");
    GradleLog.init();
    Log.info("Starting " + getParameters().getMainClass().get() +
             ": \n\t  java -jar <ToolJar>.jar " + Arrays.toString(args),
            getParameters().getMainClass().get());
    try {
      Class<?> mainClass = Class.forName(getParameters().getMainClass().get());
      mainClass.getMethod("gradleMain", String[].class).invoke(null, (Object)args);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Failed to find main class " + getParameters().getMainClass().get());
    } catch (NoSuchMethodException error) {
      // Special case: method does not exist
      throw new IllegalStateException("The main class " + getParameters().getMainClass() + " does not provide a #gradleMain method");
    } catch (ReflectiveOperationException e) {
      // pass upwards
      sneakyThrow(e.getCause());
    }
  }

  public static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
    throw (E) e;
  }
}
