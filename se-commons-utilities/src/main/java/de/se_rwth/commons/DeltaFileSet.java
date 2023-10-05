/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.Hashing;
import com.google.common.io.Closer;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Traces file updates using SHA-1 hash codes.
 *
 *
 */
public final class DeltaFileSet {
  
  /**
   * This classes builder.
   */
  public final static class Builder {
    
    private final File traceLogFile;
    private final ImmutableSet.Builder<File> files = ImmutableSet.builder();
    private Set<String> possibleFileExtensions = null;
    
    public Builder(File traceLogFile) {
      this.traceLogFile = traceLogFile;
    }
    
    public final DeltaFileSet build() throws IOException {
      return new DeltaFileSet(this.traceLogFile, this.files.build());
    }
    
    public final Builder fromFile(File file) {
      
      checkNotNull(file);

      addFile(file);

      return this;
    }
    
    public final Builder fromFiles(Set<File> files) {
      for (File file : files) {
        fromFile(file);
      }
      return this;
    }
    
    public final Builder fromFilesWithExtensions(Set<File> files, Set<String> possibleExtensions) {
      checkNotNull(possibleExtensions);
      this.possibleFileExtensions = ImmutableSet.copyOf(possibleExtensions);
      return fromFiles(files);
    }
    
    private final void addFile(File file) {
      if (file.isDirectory()) {
        for (File subFile : file.listFiles()) {
          addFile(subFile);
        }
      }
      else if (possibleFileExtensions == null || possibleFileExtensions.isEmpty() || possibleFileExtensions.contains(Files.getFileExtension(file.getName()))) {
        this.files.add(file);
      }
    }
    
  }
  
  /**
   * @return a new {@link Builder} for a file set of files with changes based on
   *         the trace log given.
   */
  public static Builder newDeltaFileSet(File traceLog) {
    return new Builder(traceLog);
  }
  
  /**
   * The set of files that have changed since the previous check.
   */
  private final Set<File> fileSet;
  
  /**
   * Private constructor permitting manual instantiation.
 * @throws IOException
   */
  private DeltaFileSet(File traceLogFile, Collection<File> fileSet) throws IOException {
    
    ImmutableSet.Builder<File> fileSetBuilder = ImmutableSet.builder();
    
      
      Properties traceLog = loadProperties(traceLogFile);
      
      for (File f : fileSet) {
        /* Ignore hidden files. */
        if (!f.getName().startsWith(".")) {
          
          String fileName = f.getAbsolutePath();
          String thisHashCode = getHashCode(f);
          String thatHashCode = traceLog.getProperty(fileName);
          
          if (!thisHashCode.equals(thatHashCode)) {
            fileSetBuilder.add(f);
            traceLog.setProperty(fileName, thisHashCode);
          }
          
        }
      }
      
      storeProperties(traceLog, traceLogFile);
      
    
    this.fileSet = fileSetBuilder.build();
    
  }
  
  /**
   * @return the set of files that have changed.
   */
  public final Set<File> getFiles() {
    return this.fileSet;
  }
  
  /**
   * Computes the hash code for the content of the given file.
   *
   * @throws IOException
   */
  private final String getHashCode(File file) throws IOException {
    
    String hashCode = "";

    hashCode = Files.hash(file, Hashing.md5()).toString();
    
    return hashCode;
  }
  
  /**
   * Loads properties from the given property file.
   *
   * @throws IOException
   */
  private Properties loadProperties(File propertiesFile) throws IOException {
    
    Properties properties = new Properties();
    Closer closer = Closer.create();
    InputStream inputStream = null;
    
    try {
      if (!propertiesFile.exists()) {
        File parent = propertiesFile.getParentFile();
        if (!parent.exists()) {
          parent.mkdirs();
        }
        propertiesFile.createNewFile();
      }
      inputStream = closer.register(Files.asByteSource(propertiesFile).openStream());
      properties.load(inputStream);
    }
    catch (IOException e) {
      closer.rethrow(e);
    }
    finally {
      closer.close();
    }
    
    return properties;
  }
  
  /**
   * Stores the given properties file to the given file.
   *
   * @throws IOException
   */
  private void storeProperties(Properties properties, File propertiesFile) throws IOException {
    
    Closer closer = Closer.create();
    OutputStream outputStream = null;
    
    try {
      propertiesFile.mkdirs();
      outputStream = closer.register(Files.asByteSink(propertiesFile).openStream());
      properties.store(outputStream, "");
    }
    catch (IOException e) {
      closer.rethrow(e);
    }
    finally {
      closer.close();
    }
    
  }
  
}
