/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * Unit tests for {@link Directories}
 *
 *
 */
public class DirectoriesTest {
  
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  
  /**
   * Tests a copy operation on two flat directories.
   */
  @Test
  public void testCopy__flatDirectoryToDirectory() throws IOException {
    
    this.folder.create();
    File sourceDirectory = this.folder.newFolder();
    File targetDirectory = this.folder.newFolder();
    File someFile = new File(sourceDirectory, "foo");
    someFile.createNewFile();
    
    Directories.copy(sourceDirectory, targetDirectory);
    
    assertThat(targetDirectory.listFiles().length, is(1));
    assertThat(targetDirectory.listFiles()[0].getName(), equalTo(someFile.getName()));
    
  }
  
  /**
   * Tests a copy operation on two nested directories.
   */
  @Test
  public void testCopy__nestedDirectoryToDirectory() throws IOException {
    
    this.folder.create();
    File sourceDirectory = this.folder.newFolder();
    File targetDirectory = this.folder.newFolder();
    File sourceSubDirectory = new File(sourceDirectory, "a/b/c");
    sourceSubDirectory.mkdirs();
    File someFile = new File(sourceSubDirectory, "foo");
    someFile.createNewFile();
    
    Directories.copy(sourceDirectory, targetDirectory);
    
    File targetSubDirectory = new File(targetDirectory, "a/b/c");
    assertThat(targetDirectory.listFiles().length, is(1));
    assertThat(targetSubDirectory.listFiles().length, is(1));
    assertThat(targetSubDirectory.listFiles()[0].getName(), equalTo(someFile.getName()));
    
  }
  
}
