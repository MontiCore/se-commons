/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.common;

import de.se_rwth.commons.logging.Log;
import org.gradle.work.ChangeType;
import org.gradle.work.FileChange;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Parsed content of an IncGenGradleCheck.txt report (written by MontiCore's IncGenGradleReporter).
 * <p>
 * Lines of the report have the following form:
 * <ul>
 *   <li>{@code <ext>:<path> <checksum>} - a (model) file read during generation, e.g. {@code mc4:} or {@code mtr:}</li>
 *   <li>{@code hwc:<path>} - a hand-written file which existed</li>
 *   <li>{@code gen:<path>} - a hand-written file which did not exist</li>
 *   <li>{@code out:<path>} - a generated file</li>
 * </ul>
 */
public class IncGenData {
  /**
   * The file extensions (= line prefixes in the report) of read model files,
   * whose content changes cause a re-generation:
   * MontiCore grammars (mc4) and transformation rules (mtr)
   */
  public static final Set<String> DEFAULT_MODEL_FILE_EXTENSIONS = Set.of("mc4", "mtr");

  /**
   * Suffix appended by the IncGenGradleReporter to read files: a md5 checksum,
   * or a placeholder for missing files / failed generations
   */
  protected static final Pattern CHECKSUM_SUFFIX =
      Pattern.compile(" ([0-9a-fA-F]{32}|not found|error during generation)?$");

  protected Set<File> generatedFiles = new TreeSet<>();
  protected Set<File> readFiles = new TreeSet<>();
  protected Set<File> existenceCheckedFiles = new TreeSet<>();
  protected File basePath;
  protected final Set<String> modelFileExtensions;

  protected boolean reportExists;

  public IncGenData() {
    reportExists = false;
    modelFileExtensions = DEFAULT_MODEL_FILE_EXTENSIONS;
  }

  // Parse from file
  public IncGenData(File file, File basePath) {
    this(file, basePath, DEFAULT_MODEL_FILE_EXTENSIONS);
  }

  /**
   * Parse from file
   *
   * @param file                the IncGenGradleCheck.txt report
   * @param basePath            the base path against which the paths of the report are resolved
   * @param modelFileExtensions the line prefixes (without colon) of read (model) files, e.g. "mc4" or "mtr"
   */
  public IncGenData(File file, File basePath, Collection<String> modelFileExtensions) {
    this.basePath = basePath;
    this.modelFileExtensions = Set.copyOf(modelFileExtensions);

    if (file.exists()) {
      assert (file.isFile());
      reportExists = true;

      try (Stream<String> lines = Files.lines(file.toPath(), Charset.defaultCharset())) {

        lines.forEach(line -> {

          checkAndAddToSet("out:", generatedFiles, line);
          checkAndAddToSet("hwc:", existenceCheckedFiles, line);
          checkAndAddToSet("gen:", existenceCheckedFiles, line);

          for (String ext : this.modelFileExtensions) {
            checkAndAddToSet(ext + ":", readFiles, line);
          }
        });
      } catch (Exception e) {
        Log.error("0xdf60e Error parsing report from previous run. Incremental check not working. \n" +
            e.getMessage());
        reportExists = false;
      }
    } else {
      Log.info("Report " + file.getAbsolutePath() + " from previous run not found. No incremental build.", this.getClass().getName());
      reportExists = false;
    }
  }

  /**
   * Adds the file of a line of the form {@code <prefix><path>[ <checksum>]} to the given store,
   * if the line starts with the prefix
   */
  protected void checkAndAddToSet(String prefix, Set<File> store, String line) throws RuntimeException {
    if (line.startsWith(prefix)) {
      String path = CHECKSUM_SUFFIX.matcher(line.substring(prefix.length())).replaceFirst("");
      try {
        store.add(Paths.get(this.basePath.getAbsolutePath(), path).toFile().getCanonicalFile());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void deleteGeneratedFiles() {
    generatedFiles.forEach(toDelete -> {
      Log.debug("\t Delete " + toDelete.getPath(), this.getClass().getName());
      toDelete.delete();
    });
  }

  protected boolean isRelevantChange(FileChange ch) {
    try {
      File f = ch.getFile().getCanonicalFile();

      return ((ch.getChangeType() == ChangeType.ADDED || ch.getChangeType() == ChangeType.REMOVED)
          && existenceCheckedFiles.contains(f)
      )
          || readFiles.contains(f);
      // TODO: Add vs full
    } catch (IOException e) {
      return true;
    }
  }


  /**
   * @param inputChanges List of file-changes from gradle
   * @return if the tool should run again, or if the previous output can be reused
   */
  public boolean isUpToDate(Stream<FileChange> inputChanges) {
    if (!reportExists) {
      // If no report exists, we assume it is NOT up-to-date
      return false;
    } else {
      Optional<FileChange> change = inputChanges
          .filter(this::isRelevantChange)
          .findAny();
      if(change.isPresent()){
        Log.info("Not UP-TO-DATE! Changed: " + change.get().getChangeType()
                + " on " + change.get().getFile().getAbsolutePath(),
            this.getClass().getName());;
      }
      return change.isEmpty();
    }
  }
}
