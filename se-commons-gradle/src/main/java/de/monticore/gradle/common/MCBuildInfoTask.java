/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.common;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Write the current version into a buildInfo.properties file.
 * Which can then be used by the UpdateChecker
 */
@DisableCachingByDefault(
        because = "Not worth caching"
)
public abstract class MCBuildInfoTask extends DefaultTask {

  @Input
  @Optional
  public abstract Property<String> getVersion();

  @OutputFile
  public abstract RegularFileProperty getBuildInfoFile();

  public MCBuildInfoTask() {
    getBuildInfoFile().convention(getProject().getLayout().getBuildDirectory()
            .file("resources/main/buildInfo.properties"));
    setGroup("mc");
  }

  @TaskAction
  public void generateBuildInfo() throws IOException {
    File file = getBuildInfoFile().get().getAsFile();
    // Provide a fallback in case the version property was not set
    String version = getVersion().getOrElse(getProject().getVersion().toString());
    // The parent directories of the file will be created if they do not exist
    FileUtils.writeStringToFile(file, "version = " + version, StandardCharsets.UTF_8);
  }
}
