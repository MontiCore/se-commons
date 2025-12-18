/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Directories}
 *
 *
 */
public class DirectoriesTest {
  /**
   * Tests a copy operation on two flat directories.
   */
  @Test
  public void testCopy__flatDirectoryToDirectory(@TempDir File sourceDirectory, @TempDir File targetDirectory) throws IOException {
    File someFile = new File(sourceDirectory, "foo");
    assertTrue(someFile.createNewFile());
    
    Directories.copy(sourceDirectory, targetDirectory);
    
    assertNotNull(targetDirectory.listFiles());
    assertThat(targetDirectory.listFiles().length, is(1));
    assertThat(targetDirectory.listFiles()[0].getName(), equalTo(someFile.getName()));
    
  }
  
  /**
   * Tests a copy operation on two nested directories.
   */
  @Test
  public void testCopy__nestedDirectoryToDirectory(@TempDir File sourceDirectory, @TempDir File targetDirectory) throws IOException {
    File sourceSubDirectory = new File(sourceDirectory, "a/b/c");
    assertTrue(sourceSubDirectory.mkdirs());
    File someFile = new File(sourceSubDirectory, "foo");
    assertTrue(someFile.createNewFile());
    
    Directories.copy(sourceDirectory, targetDirectory);
    
    File targetSubDirectory = new File(targetDirectory, "a/b/c");
    assertNotNull(targetSubDirectory.listFiles());
    assertThat(targetDirectory.listFiles().length, is(1));
    assertThat(targetSubDirectory.listFiles().length, is(1));
    assertThat(targetSubDirectory.listFiles()[0].getName(), equalTo(someFile.getName()));
    
  }
  
}
