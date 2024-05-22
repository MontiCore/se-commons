/* (c) https://github.com/MontiCore/monticore */

package de.se_rwth.commons;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * A helper class that enables some file operations <br>
 * <br>
 *
 */
public class Files {
  
  private static final int BUFFER_SIZE = 2048;
  
  private static final String TEXT_ENCODING = "UTF-8";
  
  /**
   * Creates a temporary directory.
   *
   * @return the temporary directory
   */
  public static File createTempDir() {
    return createTempDir("tmp");
  }
  
  /**
   * Creates a temporary directory with the given name as prefix and a number as
   * suffix.
   *
   * @param dirName the prefix of the directory name
   * @return the temporary directory
   */
  public static File createTempDir(String dirName) {
    String dirNamePart = System.getProperty("java.io.tmpdir")
        + File.separator + dirName;
    
    long count = 1;
    File tmpDir = new File(dirNamePart + count);
    
    while (tmpDir.exists()) {
      count++;
      tmpDir = new File(dirNamePart + count);
    }
    tmpDir.mkdir();
    
    return tmpDir;
  }
  
  public static void copyFile(File sourceFile, File targetFile)
      throws FileNotFoundException, IOException {
    writeToFile(new FileInputStream(sourceFile), targetFile);
  }
  
  public static void writeToFile(InputStream sourceStream, File targetFile)
      throws FileNotFoundException, IOException {
    
    BufferedInputStream bis = new BufferedInputStream(sourceStream);
    BufferedOutputStream bos = new BufferedOutputStream(
        new FileOutputStream(targetFile));
    int bytes = 0;
    byte[] buffer = new byte[BUFFER_SIZE];
    while ((bytes = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
      bos.write(buffer, 0, bytes);
    }
    bos.close();
    bis.close();
  }
  
  public static void writeToTextFile(StringReader source, File targetFile)
      throws FileNotFoundException, IOException {
    BufferedOutputStream bos = new BufferedOutputStream(
        new FileOutputStream(targetFile));
    OutputStreamWriter osw = new OutputStreamWriter(bos,
        TEXT_ENCODING);
    int bytes = 0;
    char[] buffer = new char[BUFFER_SIZE];
    while ((bytes = source.read(buffer, 0, BUFFER_SIZE)) != -1) {
      osw.write(buffer, 0, bytes);
    }
    osw.close();
    source.close();
  }
  
  public static byte[] getBytes(File file) throws IOException {
    byte[] bytes = new byte[(int) file.length()];
    FileInputStream fis = new FileInputStream(file);
    fis.read(bytes);
    fis.close();
    return bytes;
  }
  
  /**
   * Deletes the file and all its children.
   *
   * @param fileOrDir the file or directory to delete
   */
  public static void deleteFiles(File fileOrDir) {
    if (fileOrDir.isDirectory()) {
      for (File member : fileOrDir.listFiles()) {
        deleteFiles(member);
      }
    }
    fileOrDir.delete();
  }
  
  /**
   * Zips the input files in the output file.
   *
   * @param inputFiles the input files
   * @param pathPrefixToIgnore path prefix that should be ignored while zipping
   * the input files
   * @param outputFile the output file
   * @return the output file
   * @throws IOException
   */
  public static File zip(List<File> inputFiles, String pathPrefixToIgnore,
      File outputFile) throws IOException {
    
    byte[] buffer = new byte[BUFFER_SIZE];
    
    if (!outputFile.exists()) {
      outputFile.getParentFile().mkdirs();
      outputFile.createNewFile();
    }
    
    ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
        new FileOutputStream(outputFile)));
    zos.setLevel(Deflater.DEFAULT_COMPRESSION);
    
    for (File inputFile : inputFiles) {
      String inputFileName = inputFile.getCanonicalPath();
      if (!pathPrefixToIgnore.isEmpty()
          && inputFileName.startsWith(pathPrefixToIgnore)) {
        if (!pathPrefixToIgnore.endsWith(File.separator)) {
          pathPrefixToIgnore += File.separator;
        }
        inputFileName = inputFileName.replace(pathPrefixToIgnore, "");
      }
      
      zos.putNextEntry(new ZipEntry(inputFileName));
      
      BufferedInputStream bis = new BufferedInputStream(
          new FileInputStream(inputFile));
      
      int len;
      while ((len = bis.read(buffer, 0, BUFFER_SIZE)) > 0) {
        zos.write(buffer, 0, len);
      }
      
      bis.close();
      zos.closeEntry();
    }
    zos.close();
    
    return outputFile;
  }
  
  /**
   * Zips the input files in the output file but ignores ALL pathPrefixes
   *
   * @param inputFiles the input files
   * @param outputFile the output file
   * @return the output file
   * @throws IOException
   */
  public static File zip(List<File> inputFiles, File outputFile)
      throws IOException {
    
    byte[] buffer = new byte[BUFFER_SIZE];
    
    if (!outputFile.exists()) {
      outputFile.getParentFile().mkdirs();
      outputFile.createNewFile();
    }
    
    // check is there is any valid file
    boolean isAnyValidFile = false;
    for (File inputFile : inputFiles) {
      if (inputFile.exists()) {
        isAnyValidFile = true;
        break;
      }
    }
    
    if (isAnyValidFile) {
      ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
          new FileOutputStream(outputFile)));
      zos.setLevel(Deflater.DEFAULT_COMPRESSION);
      
      for (File inputFile : inputFiles) {
        if (inputFile.exists()) {
          String inputFileName = inputFile.getName();
          
          zos.putNextEntry(new ZipEntry(inputFileName));
          
          BufferedInputStream bis = new BufferedInputStream(
              new FileInputStream(inputFile));
          
          int len;
          while ((len = bis.read(buffer, 0, BUFFER_SIZE)) > 0) {
            zos.write(buffer, 0, len);
          }
          
          bis.close();
          zos.closeEntry();
        }
      }
      zos.close();
    }
    
    return outputFile;
  }
  
  /**
   * Unzips the input file to the output directory.
   *
   * @param inputFile the input file
   * @param outputDir the output directory
   * @return the list of unzipped files
   * @throws IOException
   */
  public static List<File> unzip(File inputFile, File outputDir)
      throws IOException {
    
    ArrayList<File> outputFiles = new ArrayList<File>();
    
    outputDir.mkdirs();
    ZipFile zipFile = new ZipFile(inputFile, ZipFile.OPEN_READ);
    
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    
    while (entries.hasMoreElements()) {
      ZipEntry entry = (ZipEntry) entries.nextElement();
      
      File outputFile = new File(outputDir, entry.getName());
      
      // sanitize path names
      String containingDir = outputDir.getCanonicalPath() + File.separator;
      String outputFileAbsolute = outputFile.getCanonicalPath();
      
      // check for directory traversal
      if (!outputFileAbsolute.startsWith(containingDir)) {
        zipFile.close();
        throw new IOException("Zip file entry contains ../ which leads to directory traversal");
      }
      
      if (entry.isDirectory()) {
        outputFile.mkdirs();
      }
      else {
        outputFile.getParentFile().mkdirs();
        
        BufferedInputStream bis = new BufferedInputStream(zipFile
            .getInputStream(entry));
        int len;
        
        byte[] data = new byte[BUFFER_SIZE];
        
        BufferedOutputStream bos = new BufferedOutputStream(
            new FileOutputStream(outputFile), BUFFER_SIZE);
        
        while ((len = bis.read(data, 0, BUFFER_SIZE)) != -1) {
          bos.write(data, 0, len);
        }
        bos.close();
        bis.close();
      }
      outputFiles.add(outputFile);
    }
    zipFile.close();
    
    return outputFiles;
  }
  
  /**
   * unzips and returns all files from the given zip file (by creating a tmp
   * file!)
   *
   * @param inputZipBytes
   * @return
   * @throws IOException
   */
  public static Collection<File> unzipFiles(byte[] inputZipBytes)
      throws IOException {
    
    return unzipFiles(inputZipBytes, Files.createTempDir());
    
  }
  
  public static Collection<File> unzipFiles(byte[] inputZipBytes, File tmpDir)
      throws IOException {
    
    File uploadDir = tmpDir;
    
    File inputDir = new File(uploadDir, "input/");
    inputDir.mkdir();
    File zipDir = new File(uploadDir, "zip/");
    zipDir.mkdir();
    
    File inputZipFile = new File(zipDir, "input.zip");
    inputZipFile.createNewFile();
    BufferedOutputStream bos = new BufferedOutputStream(
        new FileOutputStream(inputZipFile));
    bos.write(inputZipBytes);
    bos.close();
    
    Collection<File> files = Files.unzip(inputZipFile, inputDir);
    
    return files;
  }
  
  /**
   * Helper function for transforming a file into a byte array.
   *
   * @param file : given file
   * @return byte array
   * @throws IOException
   */
  public byte[] getFileAsBytes(File file) throws IOException {
    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
        file));
    byte[] bytes = new byte[(int) file.length()];
    bis.read(bytes);
    bis.close();
    return bytes;
  }
  
  /**
   * Helper function for creating an ArrayList of a file: file is read line by
   * line
   *
   * @param file : input file to transform into a collection
   * @return string collection of file
   * @throws Exception
   */
  public ArrayList<String> fileToArrayList(File file) throws IOException {
    ArrayList<String> list = new ArrayList<String>();
    String thisLine;
    
    BufferedReader in = new BufferedReader(new FileReader(file));
    while ((thisLine = in.readLine()) != null) {
      list.add(thisLine);
    }
    in.close();
    return list;
  }
  
  public List<String> stringToArrayList(String string, boolean returnDelims) {
    List<String> list = new ArrayList<String>();
    StringTokenizer tokenizer = new StringTokenizer(string, "\n",
        returnDelims);
    while (tokenizer.hasMoreTokens()) {
      list.add(tokenizer.nextToken());
    }
    
    return list;
  }
  
  /**
   * Reads a file an returns a list of lines.
   *
   * @param f
   * @return List<String> with lines.
   */
  public synchronized List<String> fileToLineList(File f) {
    List<String> list = new ArrayList<String>();
    FileReader fr = null;
    try {
      fr = new FileReader(f);
    }
    catch (FileNotFoundException e1) {
      System.err.println("test.testutils.TestFramework: Could not open "
          + "FileReader: probably too many files opened "
          + "(Unix-File-Limit is 50 Files)");
      e1.printStackTrace();
    }
    BufferedReader inputReader = new BufferedReader(fr);
    try {
      String line = "";
      line = inputReader.readLine();
      while (line != null) {
        line = line.replaceAll("\t", ""); // cut tab
        line = line.trim(); // cut whitespaces
        list.add(line);
        // System.out.println(line);
        line = inputReader.readLine();
      }
      inputReader.close();
      fr.close();
    }
    catch (Exception e) {
      System.err.println("Error while reading inputstream!");
      // This might happen if you try to read to many files
      // On Unix you have a limit of 50 Files per Thread
      // Here is only read one file at time, but MC/DSLTool seems
      // to have a lot of files opened!
      // because there is a limitation of usable file-reader.
      if (e.getMessage().contains("(Too many open files)")) {
        System.err
            .println("test.testutils.OCL-Testframework: Too many open files.");
      }
      e.printStackTrace();
    }
    finally {
      try {
        inputReader.close();
        fr.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
    return list;
  }
  
  /**
   * Transforms a Line-List to a List of Tokens.
   *
   * @param input
   * @return List<String> TokenList
   */
  @SuppressWarnings("unused")
  private synchronized List<String> lineListToTokenList(List<String> input) {
    List<String> tokenList = new LinkedList<>();
    for (String s : input) {
      StringTokenizer tokenizer = new StringTokenizer(s);
      while (tokenizer.hasMoreElements()) {
        tokenList.add(tokenizer.nextToken());
      }
    }
    return tokenList;
  }
}
