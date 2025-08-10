package com.collabboard.models;

import java.io.Serializable;

/**
 * Represents a file transfer object for sharing files between clients
 */
public class FileTransfer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String fileId;
    private String fileName;
    private String fileType;
    private byte[] data;
    private long fileSize;
    private String uploaderId;
    private String uploaderName;
    private long uploadTime;
    private int chunkIndex;
    private int totalChunks;
    private boolean isLastChunk;
    
    // Default constructor
    public FileTransfer() {
        this.uploadTime = System.currentTimeMillis();
    }
    
    // Constructor for complete file
    public FileTransfer(String fileName, String fileType, byte[] data, String uploaderId, String uploaderName) {
        this();
        this.fileName = fileName;
        this.fileType = fileType;
        this.data = data;
        this.fileSize = data.length;
        this.uploaderId = uploaderId;
        this.uploaderName = uploaderName;
        this.fileId = generateFileId();
        this.totalChunks = 1;
        this.chunkIndex = 0;
        this.isLastChunk = true;
    }
    
    // Constructor for chunked file transfer
    public FileTransfer(String fileId, String fileName, String fileType, byte[] data, 
                       String uploaderId, String uploaderName, int chunkIndex, int totalChunks, boolean isLastChunk) {
        this();
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.data = data;
        this.uploaderId = uploaderId;
        this.uploaderName = uploaderName;
        this.chunkIndex = chunkIndex;
        this.totalChunks = totalChunks;
        this.isLastChunk = isLastChunk;
    }
    
    private String generateFileId() {
        return uploaderId + "_" + System.currentTimeMillis() + "_" + fileName.hashCode();
    }
    
    // Getters and Setters
    public String getFileId() {
        return fileId;
    }
    
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public void setData(byte[] data) {
        this.data = data;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getUploaderId() {
        return uploaderId;
    }
    
    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }
    
    public String getUploaderName() {
        return uploaderName;
    }
    
    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }
    
    public long getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    public int getChunkIndex() {
        return chunkIndex;
    }
    
    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }
    
    public int getTotalChunks() {
        return totalChunks;
    }
    
    public void setTotalChunks(int totalChunks) {
        this.totalChunks = totalChunks;
    }
    
    public boolean isLastChunk() {
        return isLastChunk;
    }
    
    public void setLastChunk(boolean lastChunk) {
        isLastChunk = lastChunk;
    }
    
    /**
     * Get human-readable file size
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
    
    @Override
    public String toString() {
        return "FileTransfer{" +
                "fileId='" + fileId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", uploaderName='" + uploaderName + '\'' +
                ", chunkIndex=" + chunkIndex +
                ", totalChunks=" + totalChunks +
                '}';
    }
}
