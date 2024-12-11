/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.common;

import de.monticore.gradle.AMontiCoreConfiguration;
import de.monticore.gradle.internal.DebugClassLoader;
import de.monticore.gradle.internal.ProgressLoggerService;
import de.se_rwth.commons.logging.Log;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.*;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.work.FileChange;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Consumes:
 *  - a file collection of input models (e.g., grammars)
 *  - a configuration of dependencies, to be added to the symbol path
 *  - a hand-written code path
 *  - a hand-written models path
 *  - a templates path
 *  - a configtemplate option
 *  - a symbolpath (e.g., modelpath)
 *  - an output dir
 *  - a report dir
 *  - a debug option
 *  - further args
 *  Do NOT implement this class directly.
 *  Instead, build upon {@link MCSingleFileTask} or {@link MCAllFilesTask}
 */
public abstract class CommonMCTask extends DefaultTask {
   final static String TASK_DEBUG = "de.monticore.gradle.debug";
  final static String ORG_GRADLE_PARALLEL = "org.gradle.parallel";

  protected final ConfigurableFileCollection input = getProject().getObjects().fileCollection();

  @SkipWhenEmpty    // Implies @Incremental. Do nothing if no input model exists
  @InputFiles
  @PathSensitive(PathSensitivity.RELATIVE)
  @IgnoreEmptyDirectories
  public ConfigurableFileCollection getInput() {
    return this.input;
  }

  public void setInput(File f){
    input.from(f);
  }

  public void setInput(Iterable<File> f){
    input.from(f);
  }
  public void setInput(Object... paths){
    input.from(paths);
  }



  @Input
  @Optional
  public abstract Property<Boolean> getAddConfigurationToSymbolPath();
  @InputFiles     // Not Incremental. Any change should trigger full rebuild
  @PathSensitive(PathSensitivity.NAME_ONLY)
  @Optional
  public abstract ConfigurableFileCollection getSymbolPathConfiguration();

  @InputFiles
  @Optional
  @PathSensitive(PathSensitivity.RELATIVE)
  @Incremental  // No full rebuild, when only HWC-Directory is changed. Requires logic in Task Execution!
  @IgnoreEmptyDirectories
  public abstract ConfigurableFileCollection getHandWrittenCodeDir();

  @InputFiles
  @Optional
  @PathSensitive(PathSensitivity.RELATIVE)
  @Incremental  // No full rebuild, when only HWG-Directory is changed. Requires logic in Task Execution!
  @IgnoreEmptyDirectories
  public abstract ConfigurableFileCollection getHandWrittenGrammarDir();


  @Deprecated // old-school hwcDir = ..
  @Internal
  public void setHwcDir(File f){
    this.getHandWrittenCodeDir().setFrom(f);
  }

  @Deprecated // old-school hwcDir = [...] / hwcDir +=
  @Internal
  public void setHwcDir(Iterable<File> f){
    this.getHandWrittenCodeDir().setFrom(f);
  }

  @Deprecated
  @Internal
  public ConfigurableFileCollection getHwcDir(){
    return this.getHandWrittenCodeDir();
  }

  @Deprecated
  @Internal
  public void setHwgDir(File f){
    this.getHandWrittenGrammarDir().setFrom(f);
  }

  @Deprecated
  @Internal
  public void setHwgDir(Iterable<File> f){
    this.getHandWrittenGrammarDir().setFrom(f);
  }

  @Deprecated
  @Internal
  public ConfigurableFileCollection getHwgDir(){
    return this.getHandWrittenGrammarDir();
  }

  @InputDirectory
  @Optional
  @PathSensitive(PathSensitivity.RELATIVE)
  @IgnoreEmptyDirectories
  // Do not add "@Incremental" (unless you really know what you are doing)
  public abstract DirectoryProperty getTmplDir();

  @Input
  @Optional
  public abstract Property<String> getConfigTemplate();

  @Input
  @Optional
  public abstract Property<String> getScript();


  @InputFiles     // Not Incremental. Any change should trigger full rebuild
  @PathSensitive(PathSensitivity.NAME_ONLY)
  @Optional
  public abstract ConfigurableFileCollection getSymbolPath();

  @InputFiles
  @Incremental  // Incremental symbol path (such as MC generation the modelpath)
  @Optional
  // Absolute, since this can contain elements from the gradle jar cache, outside the project
  @PathSensitive(PathSensitivity.ABSOLUTE) // TODO: really absolute?
  public abstract ConfigurableFileCollection getIncrementalSymbolPath();


  @Input
  @Optional
  public abstract ListProperty<String> getMoreArgs();
  
  /**
   * Whether to call the Tool with the "-d" argument.
   * Defaults to the value of the {@link #TASK_DEBUG} project property.
   * Disables up-to-date checks.
   */
  @Input
  @Optional
  public abstract Property<Boolean> getDebug();
  
  /**
   * Whether to skip the WorkQueue and call the run method directly,
   * might change class-loading behavior.
   * Not guaranteed to work with the MontiCoreTemplateLoader!
   * Disables up-to-date checks.
   */
  @Input
  @Optional
  public abstract Property<Boolean> getWorkQueueDebug();

  @OutputDirectory
  public abstract DirectoryProperty getOutputDir();

  @OutputDirectory
  public abstract DirectoryProperty getReportDir();

  @InputFiles
  @Optional
  // Absolute, since this can contain elements from the gradle jar cache, outside the project
  @PathSensitive(PathSensitivity.ABSOLUTE)
  public abstract ConfigurableFileCollection getExtraClasspathElements();
  // TODO: The MCGen Task adds the grammar dependency via -mp,
  //  but we might also want to do this via a classloader



  @Inject // Used for isolated/parallel execution
  protected abstract WorkerExecutor getWorkerExecutor();

  protected final String type;
  protected final String symbolPathConfigurationName;
  protected WorkQueue workQueue;

  @Inject
  protected abstract ProgressLoggerFactory getProgressLoggerFactory();

  // Missing right now:
  // addGrammarConfig - whether to add the grammar configuration to the mp
  // includeConfigs - extra configurations added to the mp

  public CommonMCTask(String type, String symbolPathConfigurationName){
    super();
    this.type = type;
    this.symbolPathConfigurationName = symbolPathConfigurationName;
    Log.ensureInitialization(); // The IncGenCheck requires Log to be present
    // And we are unable to provide a specific Gradle-Logger due to the Log-singleton

    // Report directory is required for incremental check
    this.getReportDir().convention(
        getProject().getLayout().getBuildDirectory().dir("mc_reports/task_" + this.getName())
    );
    
    // tool debug option: convention to false, except when TASK_DEBUG is set (Project wide setting of the debug mode)
    getDebug().convention(getProject().hasProperty(TASK_DEBUG) && "true".equals(getProject().property(TASK_DEBUG)));
    this.getOutputs().doNotCacheIf("Do not cache when debugging is enabled",
        (task) -> ((CommonMCTask) task).getDebug().get());
    // work queue debug option: convention to false, must be enabled on individual tasks
    getWorkQueueDebug().convention(false);
    this.getOutputs().doNotCacheIf("Do not cache when work queue debugging is enabled",
        (task) -> ((CommonMCTask) task).getWorkQueueDebug().get());

    // Add the symbol path configuration to the file collection
    if (this.symbolPathConfigurationName != null)
      getSymbolPathConfiguration().from(getProject().getConfigurations().getByName(this.symbolPathConfigurationName));

    getAddConfigurationToSymbolPath().convention(true);
  }


  /** Create CLI-Arguments for a tool. Notice that "input" parameter is not set, must be added in subclass
   *
   * @param handlePath converts Paths String-Argument. _Must_ be used for all Paths!
   *                   It is used to print relative paths OR absolute paths
   * @return Arguments that can be handed to Tools main(...)
   */
  protected List<String> createArgList(Function<Path, String> handlePath) {
    List<String> result = new ArrayList<>();
    List<Path> symbolPath = new ArrayList<>();

    // Add all dependencies from the symbolpath configuration to the symbol path
    if (getAddConfigurationToSymbolPath().get()) {
      getSymbolPathConfiguration().forEach(it -> symbolPath.add(it.toPath()));
    }

    // check includeConfigs?

    result.add("-" + AMontiCoreConfiguration.OUT);
    result.add(handlePath.apply(getOutputDir().getAsFile().get().toPath()));

    if (getReportDir().isPresent()) {
      result.add("-" + AMontiCoreConfiguration.REPORT_BASE);
      result.add(handlePath.apply(getProject().getProjectDir().toPath()));

      // reports might differ per file
//      result.add("-" + AMontiCoreConfiguration.REPORT);
//      result.add(handlePath.apply(getReportDir().getAsFile().get().toPath()));
    }

    // Add explicit elements to the symbol path
    if (!getSymbolPath().isEmpty()) {
      getSymbolPath().forEach(it -> symbolPath.add(it.toPath()));
    }
    // Add more explicit elements to the symbol path
    if (!getIncrementalSymbolPath().isEmpty()) {
      getIncrementalSymbolPath().forEach(it -> symbolPath.add(it.toPath()));
    }



    // mp
    if (!symbolPath.isEmpty()) {
      result.add("-" + AMontiCoreConfiguration.MODELPATH);
      symbolPath.forEach(p -> result.add(handlePath.apply(p)));
    }



    // hcp
    if(!getHandWrittenCodeDir().isEmpty()) {
      result.add("-" + getHandWrittenCodeOptionString());
      getHandWrittenCodeDir().forEach(x -> result.add(handlePath.apply(x.toPath())));
    }

    // hcg  handcodedModelPath
    if(!getHandWrittenGrammarDir().isEmpty()) {
      result.add("-" + AMontiCoreConfiguration.HANDCODEDMODELPATH);
      getHandWrittenGrammarDir().forEach(x -> result.add(handlePath.apply(x.toPath())));
    }

    // configTemplate
    if(this.getConfigTemplate().isPresent()){
      result.add("-" + AMontiCoreConfiguration.CONFIGTEMPLATE);
      result.add(stripConfigTemplate(getConfigTemplate().get()));
    }

    // dev
    if(getDebug().get()){
      result.add("-" + AMontiCoreConfiguration.DEV);
    }

    // customLog

    // help -> not needed

    if(this.getTmplDir().isPresent()) {
      result.add("-" + AMontiCoreConfiguration.TEMPLATEPATH);
      result.add(handlePath.apply(getTmplDir().getAsFile().get().toPath()));
    }

    // Allows more arguments
    if(getMoreArgs().isPresent()) {
      result.addAll(this.getMoreArgs().get());
    }

    return result;
  }

  @Internal
  protected String getHandWrittenCodeOptionString() {
    return "hwc";
  }

  @Internal
  protected String getInputOptionString() {
    return "i"; // The MontiCore Generator uses "grammar" instead
  }

  protected String stripConfigTemplate(String configTemplate) {
    return configTemplate;
  }


  protected void checkParameters() throws IllegalArgumentException {
    if (getMoreArgs().isPresent()) {
      List<String> arg = this.getMoreArgs().get();
      if (arg.contains("-" + getInputOptionString())
          || arg.contains("-" + AMontiCoreConfiguration.OUT)
          || arg.contains("-" + getHandWrittenCodeOptionString())) {
        throw new IllegalArgumentException("'moreArgs' must not contain '-g' / '-o' or '-hcp' flag. Use Parameters instead");
      }
    }
  }

  /** Run a tool with the given Arguments. Depending on "debug" this is either run in a WorkAction or directly.
   *
   * @param args Arguments for the Tools main(...)
   */
  protected void startGeneration(List<String> args, String progressName) {
    if (getProgressLoggerService().isPresent()) {
      getProgressLoggerService().get().setFactory(getProgressLoggerFactory());
    }
    if (getWorkQueueDebug().get()) {
      // The work queue debug-mode disables isolation... hence static variables are shared and errors can occur, especially in parallel execution.
      if (getProject().hasProperty(ORG_GRADLE_PARALLEL) && "true".equals(getProject().property(ORG_GRADLE_PARALLEL))) {
        getLogger().warn("Gradle Parallel Execution should be disabled in Debug Mode. \n"
            + "Otherwise static variables (e.g., Mills, SymbolTables) of one Task can influence other parallel Tasks!\n"
            + "set\n\t" + ORG_GRADLE_PARALLEL + "=false\n in your <gradle.properties>");
      }
      // In debug mode, run code directly. Otherwise, breakpoints etc. do not work
      if (getExtraClasspathElements().isEmpty()) {
        // we probably should isolate the mills?
        getRunMethod().accept(args.toArray(new String[0]));
      } else {
        // we have to add the extra classpath elements to the classpath
        getLogger().warn("Note: This task is run using the DebugClassLoader with extra class path elements, "
            + "which is not fully supported (i.e. the TemplateLoader does not search in the extra class path).");
        DebugClassLoader.runDirectWithExtraClassPath(getExtraClasspathElements(), getRunMethod(), args.toArray(new String[0]));
      }
    } else {
      // In normal mode, run in a workQue. This ensures isolation & parallelization
      workQueue.submit(getToolAction(), param -> {
        param.getArgs().set(args);
        param.getProgressName().set(progressName);
        param.getExtraClasspathElements().setFrom(this.getExtraClasspathElements());
        param.getProgressLogger().set(getProgressLoggerService());
      });
    }
  }

  @Internal
  public abstract Property<ProgressLoggerService> getProgressLoggerService();

  @Internal
  protected abstract Consumer<String[]> getRunMethod();

  @Internal
  protected abstract Class<? extends AToolAction> getToolAction();

  /**
   * Set of additional input directories which are used during the inc check
   * {@link InputChanges#getFileChanges(Provider)}
   */
  @Internal
  public Set<Provider<? extends FileSystemLocation>> getOtherInputDirectories(){
    return Set.of();
  }

  /**
   * Set of additional input directories which are used during the inc check
   * {@link InputChanges#getFileChanges(FileCollection)}
   */
  @Internal
  public Set<FileCollection> getOtherInputFileCollections() {
    return Set.of(getHandWrittenCodeDir(), getHandWrittenGrammarDir(), getIncrementalSymbolPath());
  }

  protected Stream<FileChange> getStreamOfChanges(InputChanges inputChanges) {
    Stream<FileChange> result = StreamSupport.stream(inputChanges.getFileChanges(getInput()).spliterator(), false);

    for (Provider<? extends FileSystemLocation> inputLocation : getOtherInputDirectories()) {
      result = Stream.concat(result, StreamSupport.stream(inputChanges.getFileChanges(inputLocation).spliterator(), false));
    }
    for (FileCollection inputLocation : getOtherInputFileCollections()) {
      result = Stream.concat(result, StreamSupport.stream(inputChanges.getFileChanges(inputLocation).spliterator(), false));
    }
    return result;
  }

  protected void prepareRun(){
    getLogger().info("Starting Task {}", this.getName());

    checkParameters();
    this.prepareWorkQueue();
  }

  protected void prepareWorkQueue() {
    this.workQueue = getWorkerExecutor().noIsolation();
  }


  /**
   *
   * @param f file which should be checked
   * @return if file should be used as input
   */
  public boolean isInputFile(File f){
    return true;
  }

  /**
   * @param f File, which is used as tool-input
   * @return Sub-Directory in <ReportDir>, where reports for input file should be stored
   */
  protected abstract Directory getReportDirOfFile(File f);

  /**
   * @param inputFile File, which is used as tool-input
   * @return Generated 'IncGenGradleCheck' file from previous run. File might not always exist.
   */
  protected File getIncGenFile(File inputFile) {
    assert(isInputFile(inputFile));

    return getReportDirOfFile(inputFile).dir(
        inputFile.getName().toLowerCase().replace(".", "__")
    ).file("IncGenGradleCheck.txt").getAsFile();
  }

  protected static void setIfPathExists(Consumer<File> add, Path path) {
    File f = path.toFile();
    if (f.exists()) {
      add.accept(f);
    }
  }

}
