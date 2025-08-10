package com.collabboard.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for file operations
 */
public class FileUtils {
    
    public static final int CHUNK_SIZE = 64 * 1024; // 64KB chunks
    public static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB max file size
    
    /**
     * Read file as byte array
     * @param filePath Path to the file
     * @return Byte array containing file data
     * @throws IOException if file cannot be read
     */
    public static byte[] readFileAsBytes(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }
    
    /**
     * Write byte array to file
     * @param filePath Path where to write the file
     * @param data Byte array to write
     * @throws IOException if file cannot be written
     */
    public static void writeBytesToFile(String filePath, byte[] data) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, data);
    }
    
    /**
     * Get file extension from filename
     * @param fileName Name of the file
     * @return File extension (without dot)
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * Get MIME type based on file extension
     * @param extension File extension
     * @return MIME type
     */
    public static String getMimeType(String extension) {
        switch (extension.toLowerCase()) {
            case "txt":
                return "text/plain";
            case "pdf":
                return "application/pdf";
            case "doc":
            case "docx":
                return "application/msword";
            case "xls":
            case "xlsx":
                return "application/vnd.ms-excel";
            case "ppt":
            case "pptx":
                return "application/vnd.ms-powerpoint";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/avi";
            case "zip":
                return "application/zip";
            case "rar":
                return "application/rar";
            default:
                return "application/octet-stream";
        }
    }
    
    /**
     * Check if file type is allowed
     * @param extension File extension
     * @return true if file type is allowed
     */
    public static boolean isFileTypeAllowed(String extension) {
        String[] allowedTypes = {
            "txt", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "jpg", "jpeg", "png", "gif", "bmp",
            "mp3", "wav", "mp4", "avi",
            "zip", "rar", "7z"
        };
        
        for (String allowedType : allowedTypes) {
            if (allowedType.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Format file size to human readable format
     * @param bytes File size in bytes
     * @return Formatted file size string
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Split file into chunks for transfer
     * @param fileData Complete file data
     * @return List of byte arrays representing chunks
     */
    public static List<byte[]> splitFileIntoChunks(byte[] fileData) {
        List<byte[]> chunks = new ArrayList<>();
        
        for (int i = 0; i < fileData.length; i += CHUNK_SIZE) {
            int chunkSize = Math.min(CHUNK_SIZE, fileData.length - i);
            byte[] chunk = new byte[chunkSize];
            System.arraycopy(fileData, i, chunk, 0, chunkSize);
            chunks.add(chunk);
        }
        
        return chunks;
    }
    
    /**
     * Merge file chunks back into complete file
     * @param chunks List of file chunks
     * @return Complete file data
     */
    public static byte[] mergeFileChunks(List<byte[]> chunks) {
        int totalSize = chunks.stream().mapToInt(chunk -> chunk.length).sum();
        byte[] completeFile = new byte[totalSize];
        
        int offset = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, completeFile, offset, chunk.length);
            offset += chunk.length;
        }
        
        return completeFile;
    }
    
    /**
     * Create directory if it doesn't exist
     * @param directoryPath Path to the directory
     * @return true if directory exists or was created successfully
     */
    public static boolean createDirectoryIfNotExists(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Failed to create directory: " + directoryPath);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if file exists
     * @param filePath Path to the file
     * @return true if file exists
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }
    
    /**
     * Delete file
     * @param filePath Path to the file
     * @return true if file was deleted successfully
     */
    public static boolean deleteFile(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + filePath);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get safe filename by removing invalid characters
     * @param fileName Original filename
     * @return Safe filename
     */
    public static String getSafeFileName(String fileName) {
        // Remove invalid characters for file names
        return fileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
    }
    
    /**
     * Validate file size
     * @param fileSize Size of the file in bytes
     * @return true if file size is within allowed limits
     */
    public static boolean isFileSizeValid(long fileSize) {
        return fileSize > 0 && fileSize <= MAX_FILE_SIZE;
    }
}
