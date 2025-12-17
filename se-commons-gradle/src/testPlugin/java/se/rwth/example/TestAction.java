/* (c) https://github.com/MontiCore/monticore */

package se.rwth.example;

import org.gradle.workers.WorkAction;

/**
 * An example workaction, testing the various queues
 */
public abstract class TestAction implements WorkAction<TestParams> {

  // A static state
  public static StringBuilder sb = new StringBuilder();

  static {
    // Report classloading
    System.out.println("INIT TestAction"); // We expect this message in our test
  }

  @Override
  public void execute() {
    if (getParameters().getWithTestService().get()) {
      getParameters().getProgressLogger().get();
    }
    try {
      System.out.println("before wait " + getParameters().getName().get());
      Thread.sleep(1000L * getParameters().getWaitSeconds().get());
      System.out.println("pre: " + sb.toString() + " -+-+> " + getParameters().getName().get()); // we expect this message in our test
      sb.append(getParameters().getName().get());
      System.out.println("done " + getParameters().getName().get());
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

  }
}
