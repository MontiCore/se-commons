/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static com.google.common.io.Files.asCharSink;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TODO: Write me!
 *
 *
 */
public class DeltaFileSetTest {

  @Test
  public void test(@TempDir Path modelFolder) throws IOException {
    File f1 = modelFolder.resolve("blubb").toFile();
    assertTrue(f1.createNewFile());
    File f2 = modelFolder.resolve("blabb").toFile();
    assertTrue(f2.createNewFile());
    asCharSink(f1, StandardCharsets.ISO_8859_1).write("Blubb");
    asCharSink(f2, StandardCharsets.ISO_8859_1).write("Blabb");

    File traceLog = File.createTempFile(".delta", "");
    
    DeltaFileSet deltaFileSet = DeltaFileSet.newDeltaFileSet(traceLog)
        .fromFile(modelFolder.toFile())
        .build();
    
    assertNotNull(deltaFileSet);
    assertNotNull(deltaFileSet.getFiles());
    assertThat(deltaFileSet.getFiles(), hasSize(2));
    
    asCharSink(f2, StandardCharsets.ISO_8859_1).write("Blabb + Update!");

    DeltaFileSet deltaFileSet2 = DeltaFileSet.newDeltaFileSet(traceLog)
        .fromFile(modelFolder.toFile())
        .build();

    assertNotNull(deltaFileSet2);
    assertNotNull(deltaFileSet2.getFiles());
    assertThat(deltaFileSet2.getFiles(), hasSize(1));

    DeltaFileSet deltaFileSet3 = DeltaFileSet.newDeltaFileSet(traceLog)
        .fromFile(modelFolder.toFile())
        .build();
    
    assertNotNull(deltaFileSet3);
    assertNotNull(deltaFileSet3.getFiles());
    assertThat(deltaFileSet3.getFiles(), hasSize(0));
    
    asCharSink(f1, StandardCharsets.ISO_8859_1).write("Blabb + Update!");
    asCharSink(f2, StandardCharsets.ISO_8859_1).write("Blabb + Update Again!");
    
    DeltaFileSet deltaFileSet4 = DeltaFileSet.newDeltaFileSet(traceLog)
        .fromFile(modelFolder.toFile())
        .build();
    
    assertNotNull(deltaFileSet4);
    assertNotNull(deltaFileSet4.getFiles());
    assertThat(deltaFileSet4.getFiles(), hasSize(2));
    
    asCharSink(f1, StandardCharsets.ISO_8859_1).write("Blabb + Update!");
    asCharSink(f2, StandardCharsets.ISO_8859_1).write("Blabb + Update Again!");
    
    DeltaFileSet deltaFileSet5 = DeltaFileSet.newDeltaFileSet(traceLog)
        .fromFile(modelFolder.toFile())
        .build();
    
    assertNotNull(deltaFileSet5);
    assertNotNull(deltaFileSet5.getFiles());
    assertThat(deltaFileSet5.getFiles(), hasSize(0));

  }
  
}
