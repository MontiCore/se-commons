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

import java.util.Optional;

import org.apache.commons.io.FilenameUtils;

/**
 * Represents a textual source code position, i.e., consisting of line and
 * column integer values.
 *
 * @author krahn, ahorst
 */
public final class SourcePosition implements Comparable<SourcePosition>, Cloneable {
  
  /* A default source position at line 0 and column 0. */
  static SourcePosition defaultPosition = new SourcePosition(0, 0);
  
  /**
   * Getter for a default source position at line 0 and columne 0.
   * 
   * @return
   */
  public static SourcePosition getDefaultSourcePosition() {
    return defaultPosition;
  }
  
  /* source position line */
  private int line = 0;
  
  /* source position column */
  private int column = 0;
  
  /* file name */
  private Optional<String> fileName = Optional.empty();
  
  /**
   * Constructor for mc.ast.SourcePosition
   */
  SourcePosition() {
    this(0, 0);
  }
  
  /**
   * Constructor for mc.ast.SourcePosition
   * 
   * @param line
   * @param column
   */
  public SourcePosition(int line, int column) {
    setLine(line);
    setColumn(column);
  }
  
  /**
   * Constructor for mc.ast.SourcePosition
   * 
   * @param line
   * @param column
   * @param fileName
   */
  public SourcePosition(String fileName) {
    this(0, 0);
    setFileName(fileName);
  }
  
  /**
   * Constructor for mc.ast.SourcePosition
   * 
   * @param line
   * @param column
   * @param fileName
   */
  public SourcePosition(int line, int column, String fileName) {
    setLine(line);
    setColumn(column);
    setFileName(fileName);
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
  void setLine(int line) {
    this.line = line;
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
  void setColumn(int column) {
    this.column = column;
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
  public void setFileName(String fileName) {
    this.fileName = Optional.ofNullable(fileName);
  }
  
  @Override
  public boolean equals(Object o) {
    if (o instanceof SourcePosition) {
      SourcePosition other = (SourcePosition) o;
      return Integer.valueOf(this.line).equals(Integer.valueOf(other.line))
          && Integer.valueOf(this.column).equals(Integer.valueOf(other.column))
          && ((!this.fileName.isPresent() && !other.fileName.isPresent()) ||
          this.fileName.get().equals(other.fileName.get()));
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.line;
    result = prime * result + this.column;
    result = this.fileName.isPresent() ? prime * result + this.fileName.get().hashCode() : result;
    return result;
  }
  
  /**
   * The String representation of this source position of the form:
   * <code>"&lta,b&gt"</code> where <code>"a"</code> denotes the line component
   * and <code>"b"</code> denotes the column component.
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (this.fileName.isPresent() ? FilenameUtils.getName(this.fileName.get()) + ":" : "")
        + "<" + this.line + ","
        + this.column + ">";
  }
  
  @Override
  public int compareTo(SourcePosition o) {
    if (this.fileName.isPresent() && o.fileName.isPresent()) {
      int fileCompare = this.fileName.get().compareTo(o.fileName.get());
      return fileCompare == 0 ? ((this.line - o.line) == 0
          ? (this.column - o.column)
          : (this.line - o.line)) : fileCompare;
    }
    return (this.line - o.line) == 0
        ? (this.column - o.column)
        : (this.line - o.line);
  }
  
  @Override
  public SourcePosition clone() {
    return this.fileName.isPresent() ? new SourcePosition(this.line, this.column,
        this.fileName.get()) : new SourcePosition(this.line, this.column);
  }
  
}
