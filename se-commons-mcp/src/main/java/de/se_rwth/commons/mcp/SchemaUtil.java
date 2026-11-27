/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.mcp;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class SchemaUtil {

    /**
     * Builds a JSON schema of type object.
     *
     * @param properties the properties of the object, see {@link #stringProperty}
     * @param required   names of the required properties
     */
    public static JsonObject objectSchema(Map<String, JsonObject> properties, String... required) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        JsonObject props = new JsonObject();
        properties.forEach(props::add);
        schema.add("properties", props);
        JsonArray req = new JsonArray();
        Arrays.stream(required).forEach(req::add);
        schema.add("required", req);
        return schema;
    }

    /**
     * Builds a JSON schema of type string with the given description.
     */
    public static JsonObject stringProperty(String description) {
        JsonObject p = new JsonObject();
        p.addProperty("type", "string");
        p.addProperty("description", description);
        return p;
    }

    /**
     * @return the string argument {@code name}, or null if absent
     */
    public static String optionalString(JsonObject arguments, String name) {
        JsonElement e = arguments.get(name);
        if (e == null || e.isJsonNull()) {
            return null;
        }
        if (!e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString()) {
            throw new ToolHandler.InvalidParamsException("Argument '" + name + "' must be a string");
        }
        return e.getAsString();
    }

    /**
     * @return the string argument {@code name}; fails if absent
     */
    public static String requiredString(JsonObject arguments, String name) {
        String s = optionalString(arguments, name);
        if (s == null) {
            throw new ToolHandler.InvalidParamsException("Missing required argument '" + name + "'");
        }
        return s;
    }


    /**
     * Convenience for building property maps in insertion order.
     */
    public static Map<String, JsonObject> properties(Object... nameSchemaPairs) {
        Map<String, JsonObject> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < nameSchemaPairs.length; i += 2) {
            map.put((String) nameSchemaPairs[i], (JsonObject) nameSchemaPairs[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }
}
