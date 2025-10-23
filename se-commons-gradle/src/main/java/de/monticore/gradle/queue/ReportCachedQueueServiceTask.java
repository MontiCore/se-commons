/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.queue;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

/**
 * Reports the (current) state of the cachedQueueService stats in json format
 */
public abstract class ReportCachedQueueServiceTask extends DefaultTask {

  @OutputFile
  abstract public RegularFileProperty getOutputFile();

  public ReportCachedQueueServiceTask() {
    getOutputFile().convention(() -> getProject().getLayout().getBuildDirectory().file("cachedqueuestats.json").get().getAsFile());
  }

  @Internal
  public abstract Property<CachedQueueService> getSharedQueueService();

  @TaskAction
  public void execute() throws IOException {
    Files.write(getOutputFile().get().getAsFile().toPath(),
            Collections.singletonList(getSharedQueueService().get().getStats()));
  }
}
