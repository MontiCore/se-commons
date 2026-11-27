/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.mcp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * A minimal, dependency-light Model Context Protocol (MCP) server using the
 * "Streamable HTTP" transport, built on the JDK's {@link HttpServer}.
 * <p>
 * Only the subset of the transport required for tools is implemented:
 * every JSON-RPC request POSTed to {@link #endpoint} is answered with a single
 * {@code application/json} response (no SSE streams, no sessions).
 * <p>
 * Subclasses register their tools via {@link #addTool} (usually in their
 * constructor) and provide a server name and version.
 * <p>
 * Start the server using {@link #serve(String)}.
 * <p>
 * Do NOT assume this server as being secured. For local CLI use only.
 */
@SuppressWarnings("unused")
public abstract class McpHttpServer {

    /**
     * The path under which the MCP endpoint is served.
     */
    protected final String endpoint;

    /**
     * Supported protocol versions, the preferred (newest) one first.
     */
    protected static final List<String> SUPPORTED_PROTOCOL_VERSIONS = Arrays.asList("2025-11-25", "2025-06-18", "2025-03-26");

    // JSON-RPC error codes
    protected static final int PARSE_ERROR = -32700;
    protected static final int INVALID_REQUEST = -32600;
    protected static final int METHOD_NOT_FOUND = -32601;
    protected static final int INVALID_PARAMS = -32602;
    protected static final int INTERNAL_ERROR = -32603;

    protected final Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    protected final Map<String, Tool> tools = new LinkedHashMap<>();

    protected HttpServer httpServer;

    protected final CountDownLatch stopped = new CountDownLatch(1);

    public McpHttpServer(String endpoint) {
        this.endpoint = endpoint;
    }

    public McpHttpServer() {
        this("/mcp");
    }

    /**
     * @return the name reported to clients during initialization
     */
    public abstract String getServerName();

    /**
     * @return the version reported to clients during initialization
     */
    public abstract String getServerVersion();

    /**
     * @return optional usage instructions for the client/LLM, or null
     */
    @Nullable
    public String getInstructions() {
        return null;
    }

    // ---------------------------------------------------------------------------
    // Tools
    // ---------------------------------------------------------------------------


    public void addTool(String name, String description, JsonObject inputSchema, ToolHandler handler) {
        tools.put(name, new Tool(name, description, inputSchema, handler));
    }


    // ---------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------

    /**
     * Starts the server in the background.
     *
     * @param address {@code port} (binds to the loopback interface) or
     *                {@code host:port}; port 0 picks a free port
     */
    public void start(String address) throws IOException {
        InetSocketAddress socketAddress = parseAddress(address);
        httpServer = HttpServer.create(socketAddress, 0);
        httpServer.createContext("/", this::handle);
        // The default executor handles requests sequentially on a single thread,
        // which keeps non-thread-safe tool implementations (e.g. static state) safe.
        httpServer.setExecutor(null);
        httpServer.start();
    }

    /**
     * Starts the server and blocks until {@link #stop()} is called or the JVM shuts down.
     */
    public void serve(String address) throws IOException, InterruptedException {
        start(address);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        System.out.println("MCP server '" + getServerName() + "' listening on " + getUrl());
        stopped.await();
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
        stopped.countDown();
    }

    public int getPort() {
        return httpServer.getAddress().getPort();
    }

    public String getUrl() {
        InetSocketAddress a = httpServer.getAddress();
        String host = a.getAddress().getHostAddress();
        if (host.contains(":")) {
            host = "[" + host + "]";
        }
        return "http://" + host + ":" + a.getPort() + endpoint;
    }

    protected static InetSocketAddress parseAddress(String address) throws IOException {
        String a = address.trim();
        int colon = a.lastIndexOf(':');
        try {
            if (colon < 0) {
                return new InetSocketAddress(InetAddress.getLoopbackAddress(), Integer.parseInt(a));
            }
            String host = a.substring(0, colon);
            if (host.startsWith("[") && host.endsWith("]")) {
                host = host.substring(1, host.length() - 1);
            }
            return new InetSocketAddress(InetAddress.getByName(host), Integer.parseInt(a.substring(colon + 1)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid MCP address '" + address + "', expected [host:]port", e);
        }
    }

    // ---------------------------------------------------------------------------
    // HTTP transport
    // ---------------------------------------------------------------------------

    protected void handle(HttpExchange exchange) throws IOException {
        try {
            if (!endpoint.equals(exchange.getRequestURI().getPath())) {
                sendStatus(exchange, 404);
                return;
            }
            if (!isOriginAllowed(exchange.getRequestHeaders().getFirst("Origin"))) {
                sendStatus(exchange, 403);
                return;
            }
            if (!"POST".equals(exchange.getRequestMethod())) {
                // no SSE stream (GET) and no sessions (DELETE) are offered
                exchange.getResponseHeaders().add("Allow", "POST");
                sendStatus(exchange, 405);
                return;
            }
            String protocolHeader = exchange.getRequestHeaders().getFirst("MCP-Protocol-Version");
            if (protocolHeader != null && !SUPPORTED_PROTOCOL_VERSIONS.contains(protocolHeader)) {
                sendJson(exchange, 400, errorResponse(null, INVALID_REQUEST, "Unsupported MCP-Protocol-Version: " + protocolHeader));
                return;
            }

            String body;
            try (InputStream in = exchange.getRequestBody()) {
                body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            JsonElement message;
            try {
                message = JsonParser.parseString(body);
            } catch (JsonParseException e) {
                sendJson(exchange, 400, errorResponse(null, PARSE_ERROR, "Parse error: " + e.getMessage()));
                return;
            }
            if (!message.isJsonObject()) {
                sendJson(exchange, 400, errorResponse(null, INVALID_REQUEST, "Expected a single JSON-RPC message"));
                return;
            }
            JsonObject msg = message.getAsJsonObject();
            if (!msg.has("method") || !msg.has("id")) {
                // notifications and client responses only need to be acknowledged
                sendStatus(exchange, 202);
                return;
            }
            sendJson(exchange, 200, handleRequest(msg));
        } catch (RuntimeException e) {
            sendJson(exchange, 500, errorResponse(null, INTERNAL_ERROR, String.valueOf(e.getMessage())));
        } finally {
            exchange.close();
        }
    }

    /**
     * Protects against DNS rebinding: browsers send an Origin header, which
     * must point to the local machine. Non-browser clients send none.
     */
    protected boolean isOriginAllowed(@Nullable String origin) {
        if (origin == null) {
            return true;
        }
        try {
            String host = URI.create(origin).getHost();
            return "localhost".equals(host) || "127.0.0.1".equals(host) || "[::1]".equals(host);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    protected void sendStatus(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
    }

    protected void sendJson(HttpExchange exchange, int status, JsonObject json) throws IOException {
        byte[] bytes = gson.toJson(json).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    // ---------------------------------------------------------------------------
    // JSON-RPC / MCP methods
    // ---------------------------------------------------------------------------

    /**
     * Handles a JSON-RPC request (a message with method and id).
     */
    protected synchronized JsonObject handleRequest(JsonObject request) {
        JsonElement id = request.get("id");
        JsonElement methodElement = request.get("method");
        if (!methodElement.isJsonPrimitive() || !methodElement.getAsJsonPrimitive().isString()) {
            return errorResponse(id, INVALID_REQUEST, "method must be a string");
        }
        JsonObject params = request.has("params") && request.get("params").isJsonObject() ? request.getAsJsonObject("params") : new JsonObject();
        try {
            return switch (methodElement.getAsString()) {
                case "initialize" -> resultResponse(id, initialize(params));
                case "ping" -> resultResponse(id, new JsonObject());
                case "tools/list" -> resultResponse(id, listTools());
                case "tools/call" -> resultResponse(id, callTool(params));
                default -> errorResponse(id, METHOD_NOT_FOUND, "Method not found: " + methodElement.getAsString());
            };
        } catch (ToolHandler.InvalidParamsException e) {
            return errorResponse(id, INVALID_PARAMS, e.getMessage());
        }
    }

    protected JsonObject initialize(JsonObject params) {
        String requested = SchemaUtil.optionalString(params, "protocolVersion");
        JsonObject result = new JsonObject();
        result.addProperty("protocolVersion", SUPPORTED_PROTOCOL_VERSIONS.contains(requested) ? requested : SUPPORTED_PROTOCOL_VERSIONS.getFirst());
        JsonObject capabilities = new JsonObject();
        JsonObject toolCapability = new JsonObject();
        toolCapability.addProperty("listChanged", false);
        capabilities.add("tools", toolCapability);
        result.add("capabilities", capabilities);
        JsonObject serverInfo = new JsonObject();
        serverInfo.addProperty("name", getServerName());
        serverInfo.addProperty("version", getServerVersion());
        result.add("serverInfo", serverInfo);
        if (getInstructions() != null) {
            result.addProperty("instructions", getInstructions());
        }
        return result;
    }

    protected JsonObject listTools() {
        JsonArray list = new JsonArray();
        for (Tool tool : tools.values()) {
            JsonObject t = new JsonObject();
            t.addProperty("name", tool.name());
            t.addProperty("description", tool.description());
            t.add("inputSchema", tool.inputSchema());
            list.add(t);
        }
        JsonObject result = new JsonObject();
        result.add("tools", list);
        return result;
    }

    protected JsonObject callTool(JsonObject params) {
        String name = SchemaUtil.requiredString(params, "name");
        Tool tool = tools.get(name);
        if (tool == null) {
            throw new ToolHandler.InvalidParamsException("Unknown tool: " + name);
        }
        JsonObject arguments = params.has("arguments") && params.get("arguments").isJsonObject() ? params.getAsJsonObject("arguments") : new JsonObject();
        ToolResult toolResult;
        try {
            toolResult = tool.handler().call(arguments);
        } catch (ToolHandler.InvalidParamsException e) {
            throw e;
        } catch (Exception e) {
            toolResult = ToolResult.error(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        JsonObject content = new JsonObject();
        content.addProperty("type", "text");
        content.addProperty("text", toolResult.text());
        JsonArray contents = new JsonArray();
        contents.add(content);
        JsonObject result = new JsonObject();
        result.add("content", contents);
        result.addProperty("isError", toolResult.isError());
        return result;
    }

    protected static JsonObject resultResponse(JsonElement id, JsonObject result) {
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("id", id);
        response.add("result", result);
        return response;
    }

    protected static JsonObject errorResponse(@Nullable JsonElement id, int code, String message) {
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("id", id == null ? com.google.gson.JsonNull.INSTANCE : id);
        response.add("error", error);
        return response;
    }

    /**
     * A registered tool.
     */
    public record Tool(String name, String description, JsonObject inputSchema, ToolHandler handler) {
    }

}
