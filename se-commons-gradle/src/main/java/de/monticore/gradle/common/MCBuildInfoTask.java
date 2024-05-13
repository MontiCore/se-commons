/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.common;

import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Write the current version into a buildInfo.properties file.
 * Which can then be used by the UpdateChecker
 */
@CacheableTask
public abstract class MCBuildInfoTask extends DefaultTask {

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
    if (!file.getParentFile().exists()) {
      file.getParentFile().mkdirs();
    }
    file.createNewFile();
    FileUtils.writeStringToFile(file, "version = " + getProject().getVersion(), StandardCharsets.UTF_8);
  }
}
