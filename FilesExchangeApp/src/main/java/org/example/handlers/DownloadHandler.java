package org.example.handlers;

import org.example.models.FileMetadata;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class DownloadHandler implements HttpHandler {

    private final Map<String, FileMetadata> fileStore;

    public DownloadHandler(Map<String, FileMetadata> fileStore) {
        this.fileStore = fileStore;
    }

    private void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, X-File-Name");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        setCorsHeaders(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String uniqueId = path.substring(path.lastIndexOf('/') + 1);

        FileMetadata metadata = fileStore.get(uniqueId);

        if (metadata == null) {
            String response = "File not found";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }
        
        metadata.getLastAccessedTimestamp();

        Path filePath = Paths.get(metadata.getFilePath());
        if (!Files.exists(filePath)) {
            fileStore.remove(uniqueId);
            String response = "File not found";
            exchange.sendResponseHeaders(404, response.length());
             try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        // Заголовки для скачивания файла
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + metadata.getFileName() + "\"");
        exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
        exchange.sendResponseHeaders(200, Files.size(filePath));

        try (OutputStream os = exchange.getResponseBody()) {
            Files.copy(filePath, os);
        }
    }
}