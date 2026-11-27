/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.mcp;

/**
 * The (textual) result of a tool call.
 */
public record ToolResult(String text, boolean isError) {

    public static ToolResult ok(String text) {
        return new ToolResult(text, false);
    }

    public static ToolResult error(String text) {
        return new ToolResult(text, true);
    }
}
