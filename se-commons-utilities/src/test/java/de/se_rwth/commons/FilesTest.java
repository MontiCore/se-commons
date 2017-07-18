/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;


/**
 * Unit tests for {@link Files}
 * 
 * @author (last commit) $Author$
 * @version $Revision$, $Date$
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
