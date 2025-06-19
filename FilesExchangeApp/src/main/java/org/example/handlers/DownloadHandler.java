package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.models.FileMetadata;

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

    @Override
    public void handle(HttpExchange exchange) throws IOException {
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

        metadata.updateLastAccessed();

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

        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + metadata.getFileName() + "\"");
        exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
        exchange.sendResponseHeaders(200, Files.size(filePath));

        try (OutputStream os = exchange.getResponseBody()) {
            Files.copy(filePath, os);
        }

    }
}
