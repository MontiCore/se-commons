/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;


/**
 * Unit tests for {@link Files}
 *
 *
 */
public class FilesTest {
  
  @Test
  public void unzipAttack() throws URISyntaxException {
    URL zipFile = getClass().getResource("/directoryTraversal.zip");
    assertNotNull("Test file missing", zipFile);
    
    File tmp = Files.createTempDir();
    
    try {
      
      Files.unzip(new File(zipFile.toURI()), tmp);
      fail();
    }
    catch (IOException e) {
      assertEquals(e.getMessage(), "Zip file entry contains ../ which leads to directory traversal");
    }
    
    Files.deleteFiles(tmp);
  }
  
  @Test
  public void unzipFunctionality() throws URISyntaxException, IOException {
    URL zipFile = getClass().getResource("/test.zip");
    assertNotNull("Test file missing", zipFile);
    
    File tmp = Files.createTempDir();
    
    for (File entry : Files.unzip(new File(zipFile.toURI()), tmp)) {
      assertEquals(entry.getName(), "test");
    }

    Files.deleteFiles(tmp);
  }
  
}
