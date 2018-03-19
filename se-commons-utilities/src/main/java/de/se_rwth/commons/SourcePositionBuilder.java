/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import java.util.Optional;

public class SourcePositionBuilder {
  
  /* source position line */
  private int line = 0;
  
  /* source position column */
  private int column = 0;
  
  /* file name */
  private Optional<String> fileName = Optional.empty();
  
  public SourcePosition build() {
    SourcePosition res;
    if(fileName.isPresent()) {
      res = new SourcePosition(line, column, fileName.get());
    }
    else {
      return new SourcePosition(line, column);
    }
    return res;
  }
  
  /**
   * The line component of this source position.
   *
   * @return
   */
  public int getLine() {
    return this.line;
  }
  
  /**
   * Setter for the line component.
   *
   * @param line
   */
  public SourcePositionBuilder setLine(int line) {
    this.line = line;
    return this;
  }
  
  /**
   * The column component of this source position.
   *
   * @return
   */
  public int getColumn() {
    return this.column;
  }
  
  /**
   * Setter for the column component.
   *
   * @param column
   */
  public SourcePositionBuilder setColumn(int column) {
    this.column = column;
    return this;
  }
  
  /**
   * @return fileName
   */
  public Optional<String> getFileName() {
    return this.fileName;
  }
  
  /**
   * @param fileName the fileName to set
   */
  public SourcePositionBuilder setFileName(String fileName) {
    this.fileName = Optional.ofNullable(fileName);
    return this;
  }
}
