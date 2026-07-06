/* (c) https://github.com/MontiCore/monticore */
import org.apache.commons.lang3.StringUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

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
            """
            plugins {
              id 'se.rwth.example'
            }
            import se.rwth.example.ExampleTask
            import se.rwth.example.ExampleTask.WorkerKind
            tasks.register('A', ExampleTask.class, t -> {t.taskNames.add('A1'); t.taskNames.add('A2'); t.workerKind = WorkerKind.NO_ISOLATION} )
            tasks.register('B', ExampleTask.class, t -> {t.taskNames.add('B1'); t.taskNames.add('B2'); t.waitSeconds = 1; t.workerKind = WorkerKind.NO_ISOLATION} )
            B.dependsOn(A)
            """
    );

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
            """
            plugins {
              id 'se.rwth.example'
            }
            import se.rwth.example.ExampleTask
            import se.rwth.example.ExampleTask.WorkerKind
            tasks.register('A', ExampleTask.class, t -> {t.taskNames.add('A1'); t.taskNames.add('A2'); t.workerKind = WorkerKind.CL} )
            tasks.register('B', ExampleTask.class, t -> {t.taskNames.add('B1'); t.taskNames.add('B2'); t.waitSeconds = 1; t.workerKind = WorkerKind.CL} )
            B.dependsOn(A)
            """
    );

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
  @ValueSource(strings = {"8.5", "8.7", "8.14", "9.3.1","9.5.1", "9.6.1"})
  public void testSharedIsolation(String version) throws Exception {
    File projectDir = new File("build/functionalTest/shared/" + version);
    projectDir.mkdirs();
    new File(projectDir, "settings.gradle").createNewFile();
    write(new File(projectDir, "build.gradle"),
            """
            plugins {
              id 'se.rwth.example'
            }
            import se.rwth.example.ExampleTask
            import se.rwth.example.ExampleTask.WorkerKind
            tasks.register('A', ExampleTask.class, t -> {t.taskNames.add('A1'); t.taskNames.add('A2'); t.workerKind = WorkerKind.SHARED} )
            tasks.register('B', ExampleTask.class, t -> {t.taskNames.add('B1'); t.taskNames.add('B2'); t.waitSeconds = 1; t.workerKind = WorkerKind.SHARED} )
            B.dependsOn(A)
            """
    );

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
    assertTrue(o.get(2).matches("\\[B.]pre: A. -\\+-\\+> B1"), o.get(2));
    assertTrue(o.get(3).matches("\\[B.]pre: A. -\\+-\\+> B2"), o.get(3));

    String json = result.getOutput().substring(result.getOutput().indexOf("Stats[[") + "Stats[[".length(), result.getOutput().indexOf("]]Stats"));

    assertEquals(2, StringUtils.countMatches(json, "\"REUSE\""));
    assertEquals(2, StringUtils.countMatches(json, "\"CREATE\""));
    assertEquals(4, StringUtils.countMatches(json, "\"START\""));
    assertEquals(4, StringUtils.countMatches(json, "\"DONE\""));
  }
  
  @Test
  public void testSharedBuildService( ) throws Exception {
    File projectDir = new File("build/functionalTest/shared_bs/");
    projectDir.mkdirs();
    new File(projectDir, "settings.gradle").createNewFile();
    write(new File(projectDir, "build.gradle"),
            """
            plugins {
              id 'se.rwth.example'
            }
            import se.rwth.example.ExampleTask
            import se.rwth.example.ExampleTask.WorkerKind
            tasks.register('A', ExampleTask.class, t -> {t.taskNames.add('A1'); t.taskNames.add('A2'); t.workerKind = WorkerKind.SHARED; t.withTestService = true;} )
            tasks.register('B', ExampleTask.class, t -> {t.taskNames.add('B1'); t.taskNames.add('B2'); t.waitSeconds = 1; t.workerKind = WorkerKind.SHARED; t.withTestService = true;} )
            B.dependsOn(A)
            """
    );
    
    assertThrows(UnexpectedBuildFailure.class, () -> {
      BuildResult result = GradleRunner.create()
          .withGradleVersion("8.14.5")
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
