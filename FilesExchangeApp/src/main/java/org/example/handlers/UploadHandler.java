package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.example.models.FileMetadata;

public class UploadHandler implements HttpHandler {

    private final Map<String, FileMetadata> fileStore;
    private final String uploadDir;

    public UploadHandler(Map<String, FileMetadata> fileStore, String uploadDir) {
        this.fileStore = fileStore;
        this.uploadDir = uploadDir;
    }
    
    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Разрешает запросы с любого источника
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-File-Name");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Устанавливаем CORS-заголовки для любого запроса
        setCorsHeaders(exchange);

        // Отвечаем на предварительный OPTIONS-запрос
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1); // 204 No Content
            return;
        }
        
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String encodedFileName = exchange.getRequestHeaders().getFirst("X-File-Name");
        if (encodedFileName == null || encodedFileName.isEmpty()) {
            sendResponse(exchange, 400, "Bad Request: X-File-Name header is missing");
            return;
        }
        String fileName = URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8.name());

        String uniqueId = UUID.randomUUID().toString();
        Path filePath = Paths.get(uploadDir, uniqueId);

        try (InputStream is = exchange.getRequestBody()) {
            Files.copy(is, filePath);
        }

        FileMetadata metadata = new FileMetadata(fileName, filePath.toString());
        fileStore.put(uniqueId, metadata);

        // Важно! URL теперь должен быть без хоста, так как фронтенд уже знает его.
        // Или можно оставить полный, как было, это не принципиально.
        String downloadUrl = "http://localhost:8080/download/" + uniqueId;

        sendResponse(exchange, 200, downloadUrl);
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}