/* (c) https://github.com/MontiCore/monticore */

package de.se_rwth.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotated types may not be referenced except by those classes explicitly specified as friends.
 * Contact the author of the type if you wish to reference the type regardless.
 * 
 */
@Target(ElementType.TYPE)
public @interface Restricted {
  
  /**
   * Defines the set of friend classes that are explicitly permitted to use this class.
   */
  Class<?>[] friends();
}
