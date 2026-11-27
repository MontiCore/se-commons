/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.mcp;

import com.google.gson.JsonObject;

/**
 * Implementation of a tool. Thrown exceptions are reported as tool errors.
 */
@FunctionalInterface
public interface ToolHandler {
    ToolResult call(JsonObject arguments) throws Exception;

    /**
     * Thrown by tool handlers on invalid arguments; reported as JSON-RPC error.
     */
    class InvalidParamsException extends RuntimeException {
        public InvalidParamsException(String message) {
            super(message);
        }
    }
}
