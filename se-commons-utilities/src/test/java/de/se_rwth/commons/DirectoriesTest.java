/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2017, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
package de.se_rwth.commons;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


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
