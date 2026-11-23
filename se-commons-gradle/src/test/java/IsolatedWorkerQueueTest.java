/* (c) https://github.com/MontiCore/monticore */
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test checks the various kinds of isolations between workers:
 * - how many times a class is loaded
 * - how it reacts with static side effects
 *
 */
public class IsolatedWorkerQueueTest {

  private static final String GRADLE = "gradle";

  @Test
  @ResourceLock(GRADLE)
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
  @ResourceLock(GRADLE)
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
  @ResourceLock(GRADLE)
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
  @ResourceLock(GRADLE)
  public void testSharedIsolationWithConfigurationCacheReuse(@TempDir File projectDir) throws Exception {
    checkState(new File(projectDir, "settings.gradle").createNewFile());
    write(new File(projectDir, "gradle.properties"), "org.gradle.configuration-cache=true\n");
    write(new File(projectDir, "build.gradle"),
            """
            plugins {
              id 'se.rwth.example'
            }
            import se.rwth.example.ExampleTask
            tasks.register('A', ExampleTask.class)
            """
    );

    // First run: cold configuration cache, i.e. Settings/plugin apply() are run
    BuildResult first = GradleRunner.create()
            .withProjectDir(projectDir).withPluginClasspath()
            .withArguments("A", "--stacktrace").build();
    assertEquals(TaskOutcome.SUCCESS, checkNotNull(first.task(":A")).getOutcome());
    assertTrue(first.getOutput().contains("Configuration cache entry stored."), first.getOutput());

    // Second run: force task A to re-execute (as if an input changed) while the configuration
    // cache is reused, i.e. Settings/plugin apply() does NOT run again.
    BuildResult second = GradleRunner.create()
            .withProjectDir(projectDir).withPluginClasspath()
            .withArguments("A", "--rerun-tasks", "--stacktrace").build();
    assertTrue(second.getOutput().contains("Reusing configuration cache."), second.getOutput());
    assertEquals(TaskOutcome.SUCCESS, checkNotNull(second.task(":A")).getOutcome());
  }

  /**
   * Two subprojects apply the plugin independently (and the root project does not),
   * i.e., Gradle loads the plugin into two separate classloaders. The CachedQueueService is then
   * registered from one classloader and used from the other one.
   */
  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  @ResourceLock(GRADLE)
  public void testMultiProjectSeparateClassloaders(boolean configurationCache, @TempDir File projectDir) throws Exception {
    write(new File(projectDir, "settings.gradle"), "include 'sub1', 'sub2'\n");
    write(new File(projectDir, "gradle.properties"), "org.gradle.configuration-cache=" + configurationCache + "\n");
    write(new File(projectDir, "build.gradle"), "");
    for (String sub : List.of("sub1", "sub2")) {
      File subDir = new File(projectDir, sub);
      checkState(subDir.mkdirs());
      // Gradle TestKit's withPluginClasspath() injects the plugin into a single, shared classloader.
      // Instead, put the plugin onto each subproject's own buildscript classpath, with an extra
      // (distinct) entry, which forces Gradle to use separate plugin classloaders.
      checkState(new File(subDir, "extra-cp").mkdirs());
      write(new File(subDir, "build.gradle"),
              """
              import se.rwth.example.ExampleTask
              import se.rwth.example.ExampleTask.WorkerKind
              buildscript {
                dependencies {
                  classpath files(%2$s, 'extra-cp')
                }
              }
              apply plugin: 'se.rwth.example'
              tasks.register('A', ExampleTask.class, t -> {t.taskNames.add('%1$s_1'); t.workerKind = WorkerKind.SHARED; t.waitSeconds = %3$s} )
              """.formatted(sub, getPluginUnderTestClasspath(),
              // sub2's action outlives sub1:A by 2s, so it also prints after sub1:A has finished.
              // (With equal wait times, sub2 might print in the short window after Gradle
              // restored System.out/err on completion of sub1:A, but before our proxies are re-installed)
              sub.equals("sub1") ? 1 : 3));
    }

    for (int run = 0; run < 2; run++) {
      BuildResult result = GradleRunner.create()
              .withProjectDir(projectDir)
              .withArguments("A", "--rerun-tasks", "--stacktrace").build();
      System.out.println(result.getOutput());
      assertEquals(TaskOutcome.SUCCESS, checkNotNull(result.task(":sub1:A")).getOutcome());
      assertEquals(TaskOutcome.SUCCESS, checkNotNull(result.task(":sub2:A")).getOutcome());
      if (configurationCache && run == 1) {
        assertTrue(result.getOutput().contains("Reusing configuration cache."), result.getOutput());
      }
      if (run == 0) {
        // Ensure that the test setup actually results in two plugin classloaders
        assertEquals(2, Arrays.stream(result.getOutput().split("\\R"))
                .filter(l -> l.startsWith("TestPlugin-CL ")).distinct().count(), result.getOutput());
      }
      // The prefix is applied to the output of both subprojects
      List<String> o = getActionOutputs(result);
      assertTrue(o.stream().anyMatch(l -> l.startsWith("[sub1_1]pre:")), o.toString());
      assertTrue(o.stream().anyMatch(l -> l.startsWith("[sub2_1]pre:")), o.toString());
      // (even after the other task has finished)
      assertTrue(result.getOutput().contains("[sub1_1]done sub1_1"), result.getOutput());
      assertTrue(result.getOutput().contains("[sub2_1]done sub2_1"), result.getOutput());
      // and both subprojects report the stats of the same (single) service
      String out = result.getOutput();
      int first = out.indexOf("Stats[[");
      assertTrue(first >= 0, out);
      String json = out.substring(first + "Stats[[".length(), out.indexOf("]]Stats", first));
      assertEquals(2, StringUtils.countMatches(json, "\"START\""), json);
      // No warnings about multiple setups
      assertFalse(out.contains("setup multiple times"), out);
      assertFalse(out.contains("Possibly overriding"), out);
    }
  }

  @Test
  @ResourceLock(GRADLE)
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
  
  
  /**
   * @return the plugin-under-test classpath as a (groovy) list of quoted paths
   */
  protected String getPluginUnderTestClasspath() throws IOException {
    Properties properties = new Properties();
    try (InputStream in = checkNotNull(getClass().getClassLoader()
            .getResourceAsStream("plugin-under-test-metadata.properties"))) {
      properties.load(in);
    }
    return Arrays.stream(properties.getProperty("implementation-classpath").split(File.pathSeparator))
            .map(p -> "'" + p.replace("\\", "/") + "'")
            .collect(Collectors.joining(", "));
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
