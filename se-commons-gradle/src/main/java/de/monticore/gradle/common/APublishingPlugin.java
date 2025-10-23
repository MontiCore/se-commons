/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.common;

import org.gradle.api.Project;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.TaskDependencyFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

import java.util.function.Consumer;

/**
 * Common functionality for publishing plugins
 */
public abstract class APublishingPlugin {
  final SoftwareComponentFactory softwareComponentFactory;
  final TaskDependencyFactory taskDependencyFactory;

  public APublishingPlugin(SoftwareComponentFactory softwareComponentFactory, TaskDependencyFactory taskDependencyFactory) {
    this.softwareComponentFactory = softwareComponentFactory;
    this.taskDependencyFactory = taskDependencyFactory;
  }

  /**
   * Create a lazy {@link org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact} from a given
   * {@link Jar} task.
   * Provides compatibility between gradle 7 and 8
   * --
   * This is important for Gradle's dependency substitution to work,
   * otherwise relocations have to be defined by hand.
   */
  protected PublishArtifact createPublishedArtifact(TaskProvider<Jar> jarTaskProvider, Project project) {
    // The LazyPublishArtifact is a part of Gradle's internals,
    // but only accessible via the buildScript/groovy/kotlin API
    try {
      // Attempt to use the Gradle 7.4.2 constructor via reflection
      return LazyPublishArtifact.class
              .getConstructor(org.gradle.api.provider.Provider.class, String.class, FileResolver.class)
              .newInstance(jarTaskProvider, null, ((ProjectInternal) project).getFileResolver());
    } catch (ReflectiveOperationException e7) {
      // If the 7.4.2 constructor is not found, try the Gradle 8 constructor
      try {
        // Gradle 8 - the TaskDependencyFactory parameter was added
        return LazyPublishArtifact.class.getConstructor(org.gradle.api.provider.Provider.class, String.class, FileResolver.class, TaskDependencyFactory.class)
                .newInstance(jarTaskProvider, null, ((ProjectInternal) project).getFileResolver(), taskDependencyFactory);
      } catch (ReflectiveOperationException e8) {
        throw new IllegalStateException("Incompatible LazyPublishArtifact constructor in gradle " + project.getGradle().getGradleVersion(), e8);
      }
    }
  }


  /**
   * Create a new Jar task on a given source set,
   * set its archive appendix (if non-main),
   * and add it to the assemble task dependencies.
   * Further configurations MUST be done using the consumer
   */
  protected TaskProvider<Jar> createJarTaskPartial(SourceSet sourceSet, Project project, String target, Consumer<Jar> c) {
    TaskProvider<Jar> jarTask = project.getTasks().register(sourceSet.getTaskName(null, target), Jar.class,
            jar -> {
              // https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives
              jar.setPreserveFileTimestamps(false);
              jar.setReproducibleFileOrder(true);
              // And for non main source sets, also the archive appendix
              if (!SourceSet.isMain(sourceSet))
                jar.getArchiveAppendix().set(sourceSet.getName());
              // further configure it via the consumer
              c.accept(jar);
            });
    project.getTasks().named(BasePlugin.ASSEMBLE_TASK_NAME).configure(it -> it.dependsOn(jarTask));

    return jarTask;
  }

}
