package org.example;

import com.sun.net.httpserver.HttpServer;
import org.example.handlers.DownloadHandler;
import org.example.handlers.UploadHandler;
import org.example.models.FileMetadata;
import org.example.tasks.CleanupTask;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileServer {
    private static final int PORT = 8080;
    private static final String UPLOAD_DIR = "uploads";
    private static final Map<String, FileMetadata> fileStore = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get(UPLOAD_DIR));

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new CleanupTask(fileStore), 1, 1, TimeUnit.HOURS);

        // Создаем и настраиваем HTTP-сервер
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Назначаем обработчики для разных URL
        server.createContext("/upload", new UploadHandler(fileStore, UPLOAD_DIR));
        server.createContext("/download/", new DownloadHandler(fileStore));

        server.setExecutor(Executors.newFixedThreadPool(10)); // Пул потоков для обработки запросов
        server.start();

        System.out.println("Server is listening on port " + PORT);
        System.out.println("Upload endpoint: http://localhost:" + PORT + "/upload");
        System.out.println("Download endpoint: http://localhost:" + PORT + "/download/<UNIQUE_ID>");

    }
}