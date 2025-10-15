/* (c) https://github.com/MontiCore/monticore */

package de.se_rwth.codestyle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.impldep.com.google.common.base.Preconditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * This task just extracts a file from the resources
 */
@CacheableTask
public abstract class ExtractCodeStyleFileTask extends DefaultTask {
  
  @OutputFile
  abstract RegularFileProperty getDestination();
  
  @Input
  abstract Property<String> getInput();
  
  @TaskAction
  public void store() throws IOException {
    Files.copy(Preconditions.checkNotNull(
               ExtractCodeStyleFileTask.class.getClassLoader().getResourceAsStream(getInput().get())),
        getDestination().get().getAsFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
  }
}
