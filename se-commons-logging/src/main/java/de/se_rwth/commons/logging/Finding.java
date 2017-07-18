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
package de.se_rwth.commons.logging;

import java.util.Optional;

import de.se_rwth.commons.SourcePosition;

/**
 * Represents an error or warning finding and defines a default log format for
 * the finding.
 *
 * @author Robert Heim
 */
public class Finding {
  private static final String FORMAT_WITH_SOURCEPOSITION = "%s: %s";
  
  public static enum Type {
    WARNING, ERROR;
  }
  
  private Finding.Type type;
  
  private String msg;
  
  private Optional<SourcePosition> sourcePosition;
  
  /**
   * Constructor for de.se_rwth.commons.logging.Finding
   * 
   * @param type
   * @param msg
   * @param sourcePosition
   */
  public Finding(Finding.Type type, String msg, SourcePosition sourcePosition) {
    this.type = type;
    this.msg = msg;
    this.sourcePosition = Optional.ofNullable(sourcePosition);
  }
  
  /**
   * Constructor for de.se_rwth.commons.logging.Finding
   * 
   * @param type
   * @param msg
   */
  public Finding(Finding.Type type, String msg) {
    this.type = type;
    this.msg = msg;
    this.sourcePosition = Optional.empty();
  }
  
  /**
   * Formats the message in the default format.
   * 
   * @return the formatted message
   */
  public String buildMsg() {
    if (this.sourcePosition.isPresent()) {
      return String.format(FORMAT_WITH_SOURCEPOSITION, this.sourcePosition.get(), this.msg);
    }
    else {
      return this.msg;
    }
  }
  
  /**
   * @return sourcePosition
   */
  public Optional<SourcePosition> getSourcePosition() {
    return this.sourcePosition;
  }
  
  /**
   * @param sourcePosition the sourcePosition to set
   */
  public void setSourcePosition(SourcePosition sourcePosition) {
    this.sourcePosition = Optional.ofNullable(sourcePosition);
  }
  
  /**
   * @return type
   */
  public Finding.Type getType() {
    return this.type;
  }
  
  /**
   * @param type the type to set
   */
  public void setType(Finding.Type type) {
    this.type = type;
  }
  
  /**
   * @return msg
   */
  public String getMsg() {
    return this.msg;
  }
  
  /**
   * @param msg the msg to set
   */
  public void setMsg(String msg) {
    this.msg = msg;
  }
  
  public boolean isWarning() {
    return this.type == Type.WARNING;
  }
  
  public boolean isError() {
    return this.type == Type.ERROR;
  }
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return buildMsg();
  }
  
  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.msg.hashCode();
    result = prime * result + this.sourcePosition.hashCode();
    result = prime * result + this.type.hashCode();
    return result;
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Finding) {
      Finding other = (Finding) obj;
      if (this.msg.equals(other.msg)
          && this.sourcePosition.equals(other.sourcePosition)
          && this.type == other.type) {
        return true;
      }
    }
    return false;
  }
  
  public static Finding error(String message) {
    return new Finding(Type.ERROR, message);
  }
  
  public static Finding error(String message, SourcePosition sourcePosition) {
    return new Finding(Type.ERROR, message, sourcePosition);
  }
  
  public static Finding warning(String message) {
    return new Finding(Type.WARNING, message);
  }
  
  public static Finding warning(String message, SourcePosition sourcePosition) {
    return new Finding(Type.WARNING, message, sourcePosition);
  }
  
}
