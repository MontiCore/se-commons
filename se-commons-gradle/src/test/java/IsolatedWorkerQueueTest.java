/* (c) https://github.com/MontiCore/monticore */
import org.apache.commons.lang3.StringUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test checks the various kinds of isolations between workers:
 * - how many times a class is loaded
 * - how it reacts with static side effects
 *
 */
public class IsolatedWorkerQueueTest {

  @Test
  public void testNoIsolation() throws Exception {
    File projectDir = new File("build/functionalTest/noi");
    projectDir.mkdirs();
    new File(projectDir, "settings.gradle").createNewFile();
    write(new File(projectDir, "build.gradle"),
            "    plugins {\n" + "        id 'se.rwth.example' \n" + "    } \n"
                    + "    import se.rwth.example.ExampleTask \n"
                    + "    import se.rwth.example.ExampleTask.WorkerKind \n"
                    + "    tasks.register('A', ExampleTask.class, t -> {t.taskNames.add('A1'); t.taskNames.add('A2'); t.workerKind = WorkerKind.NO_ISOLATION} ) \n"
                    + "    tasks.register('B', ExampleTask.class, t -> {t.taskNames.add('B1'); t.taskNames.add('B2'); t.waitSeconds = 1; t.workerKind = WorkerKind.NO_ISOLATION} ) \n"
                    + "    B.dependsOn(A)\n" + "    \n");

    BuildResult result = GradleRunner.create().withProjectDir(projectDir).withPluginClasspath()
            .withArguments("A", "B").build();

    assertEquals(TaskOutcome.SUCCESS, result.task(":A").getOutcome());
    assertEquals(TaskOutcome.SUCCESS, result.task(":B").getOutcome());
    System.out.println(result.getOutput());
    List<String> o = getActionOutputs(result);
    assertEquals(1, getInitCount(result));
    assertEquals("pre:  -+-+> A1", o.get(0));
    assertEquals("pre: A1 -+-+> A2", o.get(1));
    assertEquals("pre: A1A2 -+-+> B1", o.get(2));
    assertEquals("pre: A1A2B1 -+-+> B2", o.get(3));
  }


  @Test
  public void testCLIsolation() throws Exception {
    File projectDir = new File("build/functionalTest/cl");
    projectDir.mkdirs();
    new File(projectDir, "settings.gradle").createNewFile();
    write(new File(projectDir, "build.gradle"),
            "    plugins {\n" + "        id 'se.rwth.example' \n" + "    } \n"
                    + "    import se.rwth.example.ExampleTask \n"
                    + "    import se.rwth.example.ExampleTask.WorkerKind \n"
                    + "    tasks.register('A', ExampleTask.class, t -> {t.taskNames.add('A1'); t.taskNames.add('A2'); t.workerKind = WorkerKind.CL} ) \n"
                    + "    tasks.register('B', ExampleTask.class, t -> {t.taskNames.add('B1'); t.taskNames.add('B2'); t.waitSeconds = 1; t.workerKind = WorkerKind.CL} ) \n"
                    + "    B.dependsOn(A)\n" + "    \n");

    BuildResult result = GradleRunner.create().withProjectDir(projectDir).withPluginClasspath()
            .withArguments("A", "B").build();


    assertEquals(TaskOutcome.SUCCESS, result.task(":A").getOutcome());
    assertEquals(TaskOutcome.SUCCESS, result.task(":B").getOutcome());
    System.out.println(result.getOutput());
    List<String> o = getActionOutputs(result);
    assertEquals(4, getInitCount(result));
    assertEquals("pre:  -+-+> A1", o.get(0));
    assertEquals("pre:  -+-+> A2", o.get(1));
    assertEquals("pre:  -+-+> B1", o.get(2));
    assertEquals("pre:  -+-+> B2", o.get(3));
  }


  @ParameterizedTest
  @ValueSource(strings = {"8.5", "8.7", "8.14"}) // gradle 9 requires class file version 61
  public void testSharedIsolation(String version) throws Exception {
    File projectDir = new File("build/functionalTest/shared/" + version);
    projectDir.mkdirs();
    new File(projectDir, "settings.gradle").createNewFile();
    write(new File(projectDir, "build.gradle"),
            "    plugins {\n" + "        id 'se.rwth.example' \n" + "    } \n"
                    + "    import se.rwth.example.ExampleTask \n"
                    + "    import se.rwth.example.ExampleTask.WorkerKind \n"
                    + "    tasks.register('A', ExampleTask.class, t -> {t.taskNames.add('A1'); t.taskNames.add('A2'); t.workerKind = WorkerKind.SHARED} ) \n"
                    + "    tasks.register('B', ExampleTask.class, t -> {t.taskNames.add('B1'); t.taskNames.add('B2'); t.waitSeconds = 1; t.workerKind = WorkerKind.SHARED} ) \n"
                    + "    B.dependsOn(A)\n" + "    \n");

    BuildResult result = GradleRunner.create()
            .withGradleVersion(version)
            .withProjectDir(projectDir).withPluginClasspath()
            .withArguments("A", "B", "--stacktrace").build();

    assertEquals(TaskOutcome.SUCCESS, result.task(":A").getOutcome());
    assertEquals(TaskOutcome.SUCCESS, result.task(":B").getOutcome());
    System.out.println(result.getOutput());
    List<String> o = getActionOutputs(result);
    assertEquals(2, getInitCount(result));
    assertEquals("[A1]pre:  -+-+> A1", o.get(0));
    assertEquals("[A2]pre:  -+-+> A2", o.get(1));
    // assert that B1 and B2 use re-used
    Assertions.assertTrue(o.get(2).matches("\\[B.]pre: A. -\\+-\\+> B1"), o.get(2));
    Assertions.assertTrue(o.get(3).matches("\\[B.]pre: A. -\\+-\\+> B2"), o.get(3));

    String json = result.getOutput().substring(result.getOutput().indexOf("Stats[[") + "Stats[[".length(), result.getOutput().indexOf("]]Stats"));

    Assertions.assertEquals(2, StringUtils.countMatches(json, "\"REUSE\""));
    Assertions.assertEquals(2, StringUtils.countMatches(json, "\"CREATE\""));
    Assertions.assertEquals(4, StringUtils.countMatches(json, "\"START\""));
    Assertions.assertEquals(4, StringUtils.countMatches(json, "\"DONE\""));
  }
  
  @Test
  
  public void testSharedBuildService( ) throws Exception {
    File projectDir = new File("build/functionalTest/shared_bs/");
    projectDir.mkdirs();
    new File(projectDir, "settings.gradle").createNewFile();
    write(new File(projectDir, "build.gradle"),
        "    plugins {\n" + "        id 'se.rwth.example' \n" + "    } \n"
            + "    import se.rwth.example.ExampleTask \n"
            + "    import se.rwth.example.ExampleTask.WorkerKind \n"
            + "    tasks.register('A', ExampleTask.class, t -> {t.taskNames.add('A1'); t.taskNames.add('A2'); t.workerKind = WorkerKind.SHARED; t.withTestService = true;} ) \n"
            + "    tasks.register('B', ExampleTask.class, t -> {t.taskNames.add('B1'); t.taskNames.add('B2'); t.waitSeconds = 1; t.workerKind = WorkerKind.SHARED; t.withTestService = true;} ) \n"
            + "    B.dependsOn(A)\n" + "    \n");
    
    Assertions.assertThrows(UnexpectedBuildFailure.class, () -> {
      BuildResult result = GradleRunner.create()
          .withGradleVersion("7.6.4")
          .withProjectDir(projectDir).withPluginClasspath()
          .withArguments("A", "B", "--stacktrace").build();
    });
    // Unfortunately, isolation and shared build services is not supported by gradle:
    // Caused by: java.lang.UnsupportedOperationException: Build services cannot be serialized
    // https://github.com/gradle/gradle/issues/28061#issuecomment-1945685806
    
  }
  
  
  protected void write(File file, String content) throws IOException {
    Files.write(file.toPath(), content.getBytes());
  }

  protected List<String> getActionOutputs(BuildResult result) {
    return Arrays.stream(result.getOutput().split(System.lineSeparator()))
            .filter(l -> l.contains("pre:")).map(l -> l.replace(System.lineSeparator(), ""))
            .collect(Collectors.toList());
  }

  protected long getInitCount(BuildResult result) {
    return Arrays.stream(result.getOutput().split(System.lineSeparator()))
            .filter(l -> l.contains("INIT TestAction")).count();
  }
}
