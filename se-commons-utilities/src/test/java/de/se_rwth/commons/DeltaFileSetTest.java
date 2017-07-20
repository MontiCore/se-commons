/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * TODO: Write me!
 *
 *
 */
public class DeltaFileSetTest {

  @Test
  public void test() throws IOException {
    
    TemporaryFolder modelFolder = new TemporaryFolder();
    modelFolder.create();
    File f1 = modelFolder.newFile("blubb");
    File f2 = modelFolder.newFile("blabb");
    Files.write("Blubb", f1, Charsets.ISO_8859_1);
    Files.write("Blabb", f2, Charsets.ISO_8859_1);

    File traceLog = File.createTempFile(".delta", "");
    
    DeltaFileSet deltaFileSet = DeltaFileSet.newDeltaFileSet(traceLog)
        .fromFile(modelFolder.getRoot())
        .build();
    
    assertNotNull(deltaFileSet);
    assertNotNull(deltaFileSet.getFiles());
    assertThat(deltaFileSet.getFiles(), hasSize(2));

    Files.write("Blabb + Update!", f2, Charsets.ISO_8859_1);

    DeltaFileSet deltaFileSet2 = DeltaFileSet.newDeltaFileSet(traceLog)
        .fromFile(modelFolder.getRoot())
        .build();

    assertNotNull(deltaFileSet2);
    assertNotNull(deltaFileSet2.getFiles());
    assertThat(deltaFileSet2.getFiles(), hasSize(1));

    DeltaFileSet deltaFileSet3 = DeltaFileSet.newDeltaFileSet(traceLog)
        .fromFile(modelFolder.getRoot())
        .build();
    
    assertNotNull(deltaFileSet3);
    assertNotNull(deltaFileSet3.getFiles());
    assertThat(deltaFileSet3.getFiles(), hasSize(0));

    Files.write("Blabb + Update!", f1, Charsets.ISO_8859_1);
    Files.write("Blabb + Update Again!", f2, Charsets.ISO_8859_1);
    
    DeltaFileSet deltaFileSet4 = DeltaFileSet.newDeltaFileSet(traceLog)
        .fromFile(modelFolder.getRoot())
        .build();
    
    assertNotNull(deltaFileSet4);
    assertNotNull(deltaFileSet4.getFiles());
    assertThat(deltaFileSet4.getFiles(), hasSize(2));

    Files.write("Blabb + Update!", f1, Charsets.ISO_8859_1);
    Files.write("Blabb + Update Again!", f2, Charsets.ISO_8859_1);
    
    DeltaFileSet deltaFileSet5 = DeltaFileSet.newDeltaFileSet(traceLog)
        .fromFile(modelFolder.getRoot())
        .build();
    
    assertNotNull(deltaFileSet5);
    assertNotNull(deltaFileSet5.getFiles());
    assertThat(deltaFileSet5.getFiles(), hasSize(0));

  }
  
}
