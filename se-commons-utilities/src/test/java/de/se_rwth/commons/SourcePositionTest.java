package de.se_rwth.commons;

import com.sun.tools.javac.util.List;
import org.junit.Test;

import static org.junit.Assert.*;

public class SourcePositionTest {

  @Test
  public void compareDoesNotThrowError(){
    List<SourcePosition> positions = List.of(
        new SourcePosition(0,0),
        new SourcePosition(1,0),
        new SourcePosition(1,0, "fileA.txt"),
        new SourcePosition(1,0, "fileB.txt"),
        null
    );

    for (int i = 0; i < positions.size(); i++) {
      SourcePosition p1 = positions.get(i);
      for (int j = 0; j < positions.size(); j++) {
        SourcePosition p2 = positions.get(j);
        if(i == j){
          assertEquals(p1, p2);
        }else{
          assertNotEquals(p1, p2);
        }
      }
    }
  }
}
