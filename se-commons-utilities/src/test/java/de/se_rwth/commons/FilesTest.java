/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Files}
 *
 *
 */
public class FilesTest {
  
  @Test
  public void unzipAttack() throws URISyntaxException {
    URL zipFile = getClass().getResource("/directoryTraversal.zip");
    assertNotNull(zipFile, "Test file missing");
    
    File tmp = Files.createTempDir();
    
    try {
      
      Files.unzip(new File(zipFile.toURI()), tmp);
      fail();
    }
    catch (IOException e) {
      assertEquals("Zip file entry contains ../ which leads to directory traversal", e.getMessage());
    }
    
    Files.deleteFiles(tmp);
  }
  
  @Test
  public void unzipFunctionality() throws URISyntaxException, IOException {
    URL zipFile = getClass().getResource("/test.zip");
    assertNotNull(zipFile, "Test file missing");
    
    File tmp = Files.createTempDir();
    
    for (File entry : Files.unzip(new File(zipFile.toURI()), tmp)) {
      assertEquals("test", entry.getName());
    }

    Files.deleteFiles(tmp);
  }
  
}
