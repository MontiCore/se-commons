/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

/**
 * An error hook handles program termination if fail quick is enabled, the
 * program is not running in interactive mode, and an error occurred.
 * <p>
 * Implementations of this interface can be set in the logger using the method
 * {@link Log#setErrorHook(IErrorHook)} and can also handle additional tasks
 * such as formatting the error messages and printing them out to the console.
 */
public interface IErrorHook {

  default void terminate() {

  }

}
