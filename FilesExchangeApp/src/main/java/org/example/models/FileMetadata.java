package org.example.models;

public class FileMetadata {
    private final String fileName;
    private final String filePath;
    private long lastAccessedTimestamp;

    public FileMetadata(String fileName, String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.lastAccessedTimestamp = System.currentTimeMillis();
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getLastAccessedTimestamp() {
        return lastAccessedTimestamp;
    }

    public void updateLastAccessed() {
        this.lastAccessedTimestamp = System.currentTimeMillis();
    }

}
