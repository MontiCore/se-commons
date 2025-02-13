package de.monticore.gradle.common;

import com.google.common.collect.Sets;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class MCOutputReportCheckTask extends DefaultTask {
  @InputFiles
  abstract ConfigurableFileCollection getInput();

  @InputDirectory // Nothing is written, only read!
  abstract DirectoryProperty getOutputDir();

  @InputDirectory // Nothing is written, only read!
  abstract DirectoryProperty getReportDir();

  private CommonMCTask originalTask;

  public void setOriginalTask(CommonMCTask originalTask) {
    this.originalTask = originalTask;
  }

  @TaskAction
  public void run() throws IOException {
    // Make sure the log is enabled and fail-quick activated
    //     (Maybe previous tests disabled it?)
    Log.init();
    Log.enableFailQuick(true);

    CommonMCTask dummyUMLPTask = originalTask; // this.getProject().getTasks().create("dummyTestTask_usedForOutputReportFrom" + this.getName(), CommonUMLPTask.class);
    /*dummyUMLPTask.getReportDir().set(this.getReportDir());
    dummyUMLPTask.getInput().from(this.getInput());
    dummyUMLPTask.getOutputDir().set(this.getOutputDir());
    dummyUMLPTask.getHwcDir().set(this.getHwcDir());
*/
    Map<File, IncGenData> incGenReports = getInput().getAsFileTree().getFiles().stream()
        .filter(dummyUMLPTask::isInputFile)
        .peek(f -> Log.debug("IncGenFile is in: " + dummyUMLPTask.getIncGenFile(f), this.getClass().getName()))
        .collect(Collectors.toMap(f -> f,
            f -> new IncGenData(dummyUMLPTask.getIncGenFile(f),
                dummyUMLPTask.getProject().getProjectDir().toPath().toAbsolutePath().toFile())));

    // There must be a report for all input-files, the only exceptions are GUIVariability Files as they don't produce any output.
    for (Map.Entry<File, IncGenData> entry : incGenReports.entrySet()) {
      if (!entry.getValue().reportExists) {
     /*   if(originalTask instanceof GuiVariabilityHandlerTask)
          Log.debug("0x438ea Didn't find report from previous run! This may lead to errors in incremental build\n" +
              "If the Input File is a GUIVariability Model this is intentional\n" +
              "Is <Reporting> enabled in UMLPTask? Is the <IncGenGradleReporter> active?\n\n" +
              "\tInput File: " + entry.getKey().getPath() + "\n" +
              "\tExpected Report in: " + dummyUMLPTask.getIncGenFile(entry.getKey()).getPath() + "\n", this.getClass().getName());
        else */
        Log.error("0x438ea Didn't find report from previous run! This leads to errors in incremental build\n" +
            "Is <Reporting> enabled in UMLPTask? Is the <IncGenGradleReporter> active?\n\n" +
            "\tInput File: " + entry.getKey().getPath() + "\n" +
            "\tExpected Report in: " + dummyUMLPTask.getIncGenFile(entry.getKey()).getPath() + "\n");
      }
    }

    // Check that generated files are disjunct. Only relevant when single files are seperate UMLPTool calls
    if(originalTask instanceof MCSingleFileTask) {
      for (Map.Entry<File, IncGenData> entry1 : incGenReports.entrySet()) {
        for (Map.Entry<File, IncGenData> entry2 : incGenReports.entrySet()) {
          if (entry1.equals(entry2)) {
            continue;
          }
          Set<File> overlap = Sets.intersection(entry1.getValue().generatedFiles, entry2.getValue().generatedFiles);
          if (!overlap.isEmpty()) {
            Log.error("0x84022 Generated Files overlap! This leads to problems with the incremental build, since the file might be deleted" +
                "Input File 1: " + entry1.getKey().getPath() + "\n" +
                "Input File 2: " + entry2.getKey().getPath() + "\n" +
                "Generated by both (according to report): " + overlap
            );
          }
        }
      }
    }


    // Check that every generated file is also reported
    {
      Set<File> reportedGenFiles = incGenReports.values().stream()
          .map(incGen -> incGen.generatedFiles)
          .flatMap(Set::stream)
          .collect(Collectors.toSet());
      Set<File> realGenFiles = getOutputDir().getAsFileTree().getFiles();

      Set<File> notReportedGenFiles = Sets.difference(realGenFiles, reportedGenFiles);
      if (!notReportedGenFiles.isEmpty()) {
        Log.error("0x3f447 There are generated files, which are not reported! \n"
            + "This breaks the incremental build of the UMLPTask\n"
            + "Report generated files using <Reporting.reportFileCreation(...)> \n"
            + "\n"
            + "The gradle task must have exclusive control over the output directory."
            + "No other gradle tasks are allowed to write to it. \n"
            + "\n"
            + "The following files are not reported:\n\t" +
            notReportedGenFiles.stream().map(File::getPath).collect(Collectors.joining("\n\t")));
      }

      List<String> allowedOutputPaths = new ArrayList<>();
      for(File f: dummyUMLPTask.getOutputs().getFiles().getFiles()){
        allowedOutputPaths.add(f.getCanonicalPath());
      }
      assert(allowedOutputPaths.contains(this.getOutputDir().get().getAsFile().getCanonicalPath()));

      for (File f : reportedGenFiles) {
        String fcanon = f.getCanonicalPath();
        if (allowedOutputPaths.stream().noneMatch(fcanon::startsWith)) {
          Log.error("0xahfh3a Reported Generated File is _not_ in the outputs tracked by gradle. \n" +
              "All generated files must be in the declared output directories of the gradle task, otherwise gradle cannot track the state correctly.\n" +
              "File: " + f.getPath()
          );
        }
      }

      Set<File> falselyReportedGenFiles = Sets.difference(reportedGenFiles, realGenFiles);
      if (!falselyReportedGenFiles.isEmpty()) {
        Log.warn("Files are reported wrongly reported as generated, which do not exist. \n" +
            "\t" + falselyReportedGenFiles.stream().map(File::getPath).collect(Collectors.joining("\n\t"))
        );
      }
    }

    // Input File must be reported, except GUIVariability (see above)
    for (Map.Entry<File, IncGenData> entry : incGenReports.entrySet()) {
      if (!entry.getValue().readFiles.contains(entry.getKey())) {
        /*if(originalTask instanceof GuiVariabilityHandlerTask)
          Log.debug("0xab127 Input file is not reported. \n" +
            "If the Input File is a GUIVariability Model this is intentional\n" +
            "You can report it with <Reporting.reportParseInputFile(...)> \n" +
            "File: " + entry.getKey(), this.getClass().getName()
          );
        else */
        Log.error("0xab127 Input file is not reported. \n" +
            "You can report it with <Reporting.reportParseInputFile(...)> \n" +
            "File: " + entry.getKey()
        );
      }
    }

    /*
    // If HWC-Dir exists, there should be HWC checks
    if(getHwcDir().isPresent()){
      Set<File> reportedHwcChecks = incGenReports.values().stream()
          .map(incGen -> incGen.existenceCheckedFiles)
          .flatMap(Set::stream)
          .collect(Collectors.toSet());
      if(reportedHwcChecks.isEmpty()){
        Log.error("0x4cf9a No file is reported as <Reporting.reportHWCExistenceCheck(...)>, even though a HWC directory exists.\n" +
            "Missing Reports lead to errors in incremental build.");
      }
    }
     */

    // All read files must exist
    {
      Set<File> readFilesThatDoNotExists = incGenReports.values().stream()
          .map(incGen -> incGen.readFiles)
          .flatMap(Set::stream)
          .filter(f -> !f.exists())
          .collect(Collectors.toSet());
      if(!readFilesThatDoNotExists.isEmpty()){
        Log.error("0x30324 Files are reported as read, which do not exist \n" +
            readFilesThatDoNotExists);
      }
    }

    // All read / checked files _must_ be declared as input in UMLPTask
    // Otherwise the incremental build is not working!
    {
      Set<File> reportedInputFiles = incGenReports.values().stream()
          .map(incGen -> Sets.union(incGen.existenceCheckedFiles, incGen.readFiles))
          .flatMap(Set::stream)
          .collect(Collectors.toSet());
      Set<File> gradleInput = dummyUMLPTask.getOtherInputDirectories().stream()
          .filter(Provider::isPresent)
          .map(Provider::get)
          .map(FileSystemLocation::getAsFile)
          .collect(Collectors.toSet());
      Set<File> gradleInputFiles = gradleInput.stream()
          .flatMap(f -> FileUtils.listFiles(f, null, true).stream())
          .collect(Collectors.toSet());
      gradleInputFiles.addAll(getInput().getAsFileTree().getFiles());

      for(File reportedInputFile: reportedInputFiles){
        if(reportedInputFile.exists() && !gradleInputFiles.contains(reportedInputFile)){
          Log.error("0x4aff2 Input File is not declared as input in Gradle UMLPTask. Incremental build is not working!\n"
              + reportedInputFile);
        }
        if(!reportedInputFile.exists()){
          Path inputPath = reportedInputFile.toPath().toAbsolutePath().normalize();
          boolean foundDir = gradleInput.stream()
              .filter(File::isDirectory)
              .map(File::toPath)
              .map(Path::toAbsolutePath)
              .map(Path::normalize)
              .anyMatch(inputPath::startsWith);
          if(!foundDir){
            Log.error("0x5c281 Directory of reported input file is not part of declared input of UMLPTask.\n"
                + "This leads to errors in incremental build, since gradle doesn't track changes of file.\n"
                + "Undeclared input file: " + reportedInputFile);
          }
        }
      }
    }
  }

}
