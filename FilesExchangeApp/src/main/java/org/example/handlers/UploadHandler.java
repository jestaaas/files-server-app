package org.example.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.models.FileMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;



public class UploadHandler implements HttpHandler {

    private final Map<String, org.example.models.FileMetadata> fileStore;
    private final String uploadDir;

    public UploadHandler(Map<String, FileMetadata> fileStore, String uploadDir) {
        this.fileStore = fileStore;
        this.uploadDir = uploadDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
        }

        String fileName = exchange.getRequestHeaders().getFirst("X-File-Name");
        if (fileName == null || fileName.isEmpty()) {
            sendResponse(exchange, 400, "Bad Request: X-File-Name header is missing");
        }

        String uniqueFileName = UUID.randomUUID().toString();
        Path filePath = Paths.get(uploadDir, uniqueFileName);

        try (InputStream is = exchange.getRequestBody()) {
            Files.copy(is, filePath);
        }

        FileMetadata metadata = new FileMetadata(fileName, filePath.toString());
        fileStore.put(uniqueFileName, metadata);

        String downloadUrl = "http://" + exchange.getLocalAddress().getHostName()
                + ":" + exchange.getLocalAddress().getPort()
                + "/download/" + uniqueFileName;

        sendResponse(exchange, 200, downloadUrl);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        exchange.sendResponseHeaders(statusCode, responseText.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()){
            os.write(responseText.getBytes());
        }
    }

}
