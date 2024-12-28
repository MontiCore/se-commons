package de.monticore.gradle.common;

import de.monticore.gradle.AMontiCoreConfiguration;
import org.apache.commons.io.FileUtils;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.InputChanges;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Reacts to any changes to an input set by starting generation on ALL inputs.
 * Override {@link MCAllFilesTask#createArgList(Function)}
 * and add each input file via {@link MCAllFilesTask#getInputFilesAsStream()}
 * to the arguments
 */
public abstract class MCAllFilesTask extends CommonMCTask {
  public MCAllFilesTask(String type, String symbolPathConfigurationName) {
    super(type, symbolPathConfigurationName);
  }

  @Override
  protected Directory getReportDirOfFile(File f) {
    return getReportDir().get();
  }

  @Internal
  protected Stream<File> getInputFilesAsStream() {
    return this.getInput().getAsFileTree().getFiles().stream()
            .filter(File::isFile)
            .filter(this::isInputFile);
  };

  /**
   * Delete all outputs of previous runs
   */
  protected void cleanOutputs() throws IOException {
    FileUtils.deleteDirectory(getOutputDir().get().getAsFile());
    FileUtils.deleteDirectory(getReportDir().get().getAsFile());
  }


  protected boolean shouldRunAgain(InputChanges inputChanges){
    return getInputFilesAsStream()
            .map(this::getIncGenFile)
            .map(incGenFile -> new IncGenData(incGenFile, this.getProject().getProjectDir()))
            .map(incGenData -> incGenData.isUpToDate(getStreamOfChanges(inputChanges)))
            .anyMatch(upToDate -> !upToDate);
  }

  @TaskAction
  public void run(InputChanges inputChanges) throws IOException {
    prepareRun();

    boolean shouldRunAgain = !inputChanges.isIncremental() || shouldRunAgain(inputChanges);
    if (shouldRunAgain) {
      getLogger().info("*NOT* UP-TO-DATE, starting generation process");

      cleanOutputs();

      Path cwd = getProject().getProjectDir().toPath().toAbsolutePath();
      getLogger().debug("Starting Tool with args: \n{} for {}",
              String.join(" ", createArgList(p -> "." + File.separator +
                      (cwd.getRoot().equals(p.getRoot()) ? cwd.relativize(p) : p.toAbsolutePath()))),
              this.getName());

      List<String> args = createArgList(p -> p.toAbsolutePath().toString());
      startGeneration(args, this.getName());
    } else {
      getLogger().info("UP-TO-DATE, no action required");
    }
  }

  /**
   * @param handlePath function that serializes path
   * @return Arguments that can be used to start CLI-Tool
   */
  protected List<String> createArgList(Function<Path, String> handlePath) {
    List<String> result = super.createArgList(handlePath);

    getInputFilesAsStream()
        .forEach(f -> {
          result.add("-" + getInputOptionString());
          result.add(handlePath.apply(f.toPath()));
        });

    result.add("-" + AMontiCoreConfiguration.REPORT);
    result.add(handlePath.apply(getReportDir().get().getAsFile().toPath()));

    return result;
  }


}
