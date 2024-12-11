/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.common;

import de.monticore.gradle.AMontiCoreConfiguration;
import org.apache.commons.io.FileUtils;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileType;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.ChangeType;
import org.gradle.work.InputChanges;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

/**
 * Reacts to changes to an input set by starting generation on the changed
 *  inputs (and their dependants).
 * A new TaskAction is launched for every new generation
 */
abstract public class MCSingleFileTask extends CommonMCTask {

  public MCSingleFileTask(String type, String symbolPathConfigurationName) {
    super(type, symbolPathConfigurationName);
  }

  @Override
  protected Directory getReportDirOfFile(File f) {
    String pathName = getProject().getProjectDir().toPath().toAbsolutePath().relativize(f.toPath()).toString();
    return getReportDir().dir(pathName.replace(".", "__")).get();
  }

  protected void deletePreviousOutput(File f, IncGenData lastRun) {
    getLogger().debug("Deleting generated artifacts from previous run of {}", f.getName());
    lastRun.deleteGeneratedFiles();

    // Deleting old reports.
    try {
      FileUtils.deleteDirectory(this.getReportDirOfFile(f).getAsFile());
    } catch (IOException e) {
      getLogger().warn("Error deleting old reports. " +
          "This could cause errors in upcoming incremental builds." +
          "Affected directory: {}", this.getReportDirOfFile(f));
    }
  }

  protected void runOnSingleInputFile(File f, InputChanges changedInput) {
    assert f.exists();
    assert f.isFile();
    assert isInputFile(f);

    IncGenData lastRun = new IncGenData(getIncGenFile(f), this.getProject().getProjectDir());

    if (!changedInput.isIncremental()  || !lastRun.isUpToDate(getStreamOfChanges(changedInput))) {
      getLogger().info("{} is *NOT* UP-TO-DATE, starting generation process",
          f.getName());

      deletePreviousOutput(f, lastRun);
      startGeneration(f);
    } else {
      getLogger().info(f.getName() + " is UP-TO-DATE, no action required", this.getClass().getName());
    }
  }

  private void startGeneration(File f) {
    Path cwd = getProject().getProjectDir().toPath().toAbsolutePath();
    getLogger().debug("Starting Tool: \n{} for {}",
            String.join(" ", createArgList(f.toPath(), p -> "." + File.separator +
            (cwd.getRoot().equals(p.getRoot()) ? cwd.relativize(p) : p.toAbsolutePath()))),
            this.getName());

    List<String> args = this.createArgList(f.toPath(), p -> p.toAbsolutePath().toString());
    startGeneration(args, f.getName());
  }

  /**
   * @param filePath Path of InputFile
   * @return Arguments that can be used to start CLI-Tool
   */
  protected List<String> createArgList(Path filePath, Function<Path, String> handlePath) {
    assert (filePath.toFile().isFile());
    assert (isInputFile(filePath.toFile()));

    List<String> result = super.createArgList(handlePath);

    // Add the report dirs
    result.add("-" + AMontiCoreConfiguration.REPORT);
    result.add(handlePath.apply(this.getReportDirOfFile(filePath.toFile()).getAsFile().toPath()));

    result.add("-" + getInputOptionString());
    result.add(handlePath.apply(filePath));

    return result;
  }


  @TaskAction
  public void run(InputChanges inputChanges) {
    prepareRun();

    // Remove Artifacts from deleted inputs
    inputChanges.getFileChanges(this.getInput()).forEach(ch -> {
      if (ch.getChangeType() == ChangeType.REMOVED
          && isInputFile(ch.getFile())
          && ch.getFileType() == FileType.FILE) {
        this.deletePreviousOutput(ch.getFile(), new IncGenData(getIncGenFile(ch.getFile()), getProject().getProjectDir().toPath().toAbsolutePath().toFile()));
      }
    });

    // Generate from input
    getInput().getAsFileTree().getFiles().stream()
        .filter(this::isInputFile)
        .forEach(x -> runOnSingleInputFile(x, inputChanges));
  }

}
