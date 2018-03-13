/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import java.util.Optional;

/**
 * Represents the truth values of a ternary logic value, that is, true, false
 * and undefined.
 *
 *
 */
public enum TernaryLogicValue {
  
  TRUE(true), FALSE(false), UNDEFINED(null);
  
  private final Boolean booleanValue;
  
  /**
   * Constructor for cc.clarc.lang.architecture.symboltable.TernaryLogicValue
   */
  private TernaryLogicValue(Boolean booleanValue) {
    this.booleanValue = booleanValue;
  }
  
  public final Optional<Boolean> asBoolean() {
    return Optional.ofNullable(this.booleanValue);
  }
  
}
