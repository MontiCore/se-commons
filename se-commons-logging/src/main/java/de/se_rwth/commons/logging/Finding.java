/* (c) https://github.com/MontiCore/monticore */
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
  private Optional<SourcePosition> sourcePositionEnd;

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
    this.sourcePositionEnd = Optional.empty();
  }

  /**
   * Constructor for de.se_rwth.commons.logging.Finding
   *
   * @param type
   * @param msg
   * @param start
   * @param end
   */
  public Finding(Finding.Type type, String msg, SourcePosition start, SourcePosition end) {
    this.type = type;
    this.msg = msg;
    this.sourcePosition = Optional.ofNullable(start);
    this.sourcePositionEnd = Optional.ofNullable(end);
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
    this.sourcePositionEnd = Optional.empty();
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
   * @return sourcePositionEnd
   */
  public Optional<SourcePosition> getSourcePositionEnd() {
    return this.sourcePositionEnd;
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

  public static Finding error(String message, SourcePosition start, SourcePosition end) {
    return new Finding(Type.ERROR, message, start, end);
  }
  
  public static Finding warning(String message) {
    return new Finding(Type.WARNING, message);
  }
  
  public static Finding warning(String message, SourcePosition sourcePosition) {
    return new Finding(Type.WARNING, message, sourcePosition);
  }

  public static Finding warning(String message, SourcePosition start, SourcePosition end) {
    return new Finding(Type.WARNING, message, start, end);
  }
  
}
