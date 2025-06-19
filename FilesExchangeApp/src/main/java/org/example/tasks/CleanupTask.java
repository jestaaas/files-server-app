package org.example.tasks;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.example.models.FileMetadata;

public class CleanupTask implements Runnable {

    private final Map<String, FileMetadata> fileStore;
    private static final long EXPIRATION_TIME = TimeUnit.DAYS.toMillis(30);

    public CleanupTask(Map<String, FileMetadata> fileStore) {
        this.fileStore = fileStore;
    }

    @Override
    public void run() {

        long now = System.currentTimeMillis();

        Iterator<Map.Entry<String, FileMetadata>> iterator = fileStore.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, FileMetadata> entry = iterator.next();
            FileMetadata metadata = entry.getValue();

            if (now - metadata.getLastAccessedTimestamp() > EXPIRATION_TIME) {
                try {
                    Files.deleteIfExists(Paths.get(metadata.getFilePath()));
                    iterator.remove();
                } catch (Exception e) {
                    System.err.println("Error deleting file: " + metadata.getFilePath());
                    e.printStackTrace();
                }
            }
        }
    }
}
