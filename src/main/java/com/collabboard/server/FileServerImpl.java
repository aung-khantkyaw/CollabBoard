package com.collabboard.server;

import com.collabboard.interfaces.FileService;
import com.collabboard.interfaces.ClientCallback;
import com.collabboard.models.FileTransfer;
import com.collabboard.utils.FileUtils;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;

/**
 * Server implementation of the FileService interface
 */
public class FileServerImpl extends UnicastRemoteObject implements FileService {
    
    private final Map<String, FileTransfer> storedFiles;
    private final Map<String, ClientCallback> fileClients;
    private final Properties config;
    private final String storageDirectory;
    private final long maxFileSize;
    
    public FileServerImpl(Properties config) throws RemoteException {
        super();
        this.config = config;
        this.storedFiles = new ConcurrentHashMap<>();
        this.fileClients = new ConcurrentHashMap<>();
        this.storageDirectory = config.getProperty("file.storage.directory", "./files");
        this.maxFileSize = Long.parseLong(config.getProperty("file.max.size", "52428800")); // 50MB
        
        // Create storage directory if it doesn't exist
        FileUtils.createDirectoryIfNotExists(storageDirectory);
        
        // Load existing files from storage directory
        loadExistingFiles();
        
        System.out.println("FileServer initialized - Storage: " + storageDirectory);
    }
    
    @Override
    public String uploadFile(FileTransfer fileData) throws RemoteException {
        if (fileData == null) {
            throw new RemoteException("File data cannot be null");
        }
        
        // Validate file
        if (!FileUtils.isFileSizeValid(fileData.getFileSize())) {
            throw new RemoteException("File size exceeds maximum allowed size: " + 
                                    FileUtils.formatFileSize(maxFileSize));
        }
        
        String extension = FileUtils.getFileExtension(fileData.getFileName());
        if (!FileUtils.isFileTypeAllowed(extension)) {
            throw new RemoteException("File type not allowed: " + extension);
        }
        
        try {
            String fileId = fileData.getFileId();
            if (fileId == null) {
                fileId = generateFileId(fileData);
                fileData.setFileId(fileId);
            }
            
            // Save file to disk
            String filePath = storageDirectory + File.separator + fileId + "." + extension;
            FileUtils.writeBytesToFile(filePath, fileData.getData());
            
            // Store file metadata
            FileTransfer metadata = new FileTransfer();
            metadata.setFileId(fileId);
            metadata.setFileName(fileData.getFileName());
            metadata.setFileType(extension);
            metadata.setFileSize(fileData.getFileSize());
            metadata.setUploaderId(fileData.getUploaderId());
            metadata.setUploaderName(fileData.getUploaderName());
            metadata.setUploadTime(System.currentTimeMillis());
            
            storedFiles.put(fileId, metadata);
            
            System.out.println("File uploaded: " + fileData.getFileName() + 
                             " (" + FileUtils.formatFileSize(fileData.getFileSize()) + 
                             ") by " + fileData.getUploaderName());
            
            return fileId;
            
        } catch (IOException e) {
            throw new RemoteException("Failed to save file: " + e.getMessage());
        }
    }
    
    @Override
    public FileTransfer downloadFile(String fileId) throws RemoteException {
        if (fileId == null || fileId.trim().isEmpty()) {
            throw new RemoteException("File ID cannot be null or empty");
        }
        
        FileTransfer metadata = storedFiles.get(fileId);
        if (metadata == null) {
            return null;
        }
        
        try {
            String extension = FileUtils.getFileExtension(metadata.getFileName());
            String filePath = storageDirectory + File.separator + fileId + "." + extension;
            
            if (!FileUtils.fileExists(filePath)) {
                System.err.println("File not found on disk: " + filePath);
                return null;
            }
            
            byte[] fileData = FileUtils.readFileAsBytes(filePath);
            
            FileTransfer fileTransfer = new FileTransfer(
                metadata.getFileName(),
                metadata.getFileType(),
                fileData,
                metadata.getUploaderId(),
                metadata.getUploaderName()
            );
            fileTransfer.setFileId(fileId);
            fileTransfer.setUploadTime(metadata.getUploadTime());
            
            System.out.println("File downloaded: " + metadata.getFileName() + " by client");
            
            return fileTransfer;
            
        } catch (IOException e) {
            throw new RemoteException("Failed to read file: " + e.getMessage());
        }
    }
    
    @Override
    public List<FileTransfer> getSharedFiles() throws RemoteException {
        return new ArrayList<>(storedFiles.values());
    }
    
    @Override
    public void shareFile(String fileId, String uploaderId) throws RemoteException {
        if (fileId == null || uploaderId == null) {
            throw new RemoteException("File ID and uploader ID cannot be null");
        }
        
        FileTransfer metadata = storedFiles.get(fileId);
        if (metadata == null) {
            throw new RemoteException("File not found: " + fileId);
        }
        
        System.out.println("File shared: " + metadata.getFileName() + " by " + metadata.getUploaderName());
        
        // Notify all clients about the shared file
        notifyAllFileClients(callback -> callback.onFileShared(metadata));
    }
    
    @Override
    public boolean deleteFile(String fileId, String userId) throws RemoteException {
        if (fileId == null || userId == null) {
            throw new RemoteException("File ID and user ID cannot be null");
        }
        
        FileTransfer metadata = storedFiles.get(fileId);
        if (metadata == null) {
            return false;
        }
        
        // Check if user has permission to delete (only uploader can delete)
        if (!metadata.getUploaderId().equals(userId)) {
            throw new RemoteException("Permission denied: Only the uploader can delete this file");
        }
        
        try {
            // Remove from memory
            storedFiles.remove(fileId);
            
            // Delete from disk
            String extension = FileUtils.getFileExtension(metadata.getFileName());
            String filePath = storageDirectory + File.separator + fileId + "." + extension;
            boolean deleted = FileUtils.deleteFile(filePath);
            
            if (deleted) {
                System.out.println("File deleted: " + metadata.getFileName() + " by " + userId);
                
                // Notify all clients about file deletion
                notifyAllFileClients(callback -> callback.onFileDeleted(fileId, userId));
            }
            
            return deleted;
            
        } catch (Exception e) {
            throw new RemoteException("Failed to delete file: " + e.getMessage());
        }
    }
    
    @Override
    public boolean uploadFileChunk(FileTransfer fileChunk) throws RemoteException {
        // For large file uploads - implement chunked upload logic
        if (fileChunk == null) {
            throw new RemoteException("File chunk cannot be null");
        }
        
        try {
            String fileId = fileChunk.getFileId();
            String tempDir = storageDirectory + File.separator + "temp";
            FileUtils.createDirectoryIfNotExists(tempDir);
            
            String chunkPath = tempDir + File.separator + fileId + "_chunk_" + fileChunk.getChunkIndex();
            FileUtils.writeBytesToFile(chunkPath, fileChunk.getData());
            
            System.out.println("Chunk uploaded: " + fileChunk.getChunkIndex() + "/" + 
                             fileChunk.getTotalChunks() + " for file: " + fileChunk.getFileName());
            
            // If this is the last chunk, assemble the complete file
            if (fileChunk.isLastChunk()) {
                return assembleChunkedFile(fileChunk);
            }
            
            return true;
            
        } catch (IOException e) {
            throw new RemoteException("Failed to upload chunk: " + e.getMessage());
        }
    }
    
    @Override
    public boolean fileExists(String fileId) throws RemoteException {
        return storedFiles.containsKey(fileId);
    }
    
    @Override
    public FileTransfer getFileMetadata(String fileId) throws RemoteException {
        return storedFiles.get(fileId);
    }
    
    @Override
    public void registerFileClient(ClientCallback client, String userId) throws RemoteException {
        if (client == null || userId == null) {
            throw new RemoteException("Client callback and user ID cannot be null");
        }
        
        fileClients.put(userId, client);
        System.out.println("File client registered: " + userId + 
                          " (Total file clients: " + fileClients.size() + ")");
        
        // Send current file list to new client
        try {
            List<FileTransfer> files = getSharedFiles();
            for (FileTransfer file : files) {
                client.onFileShared(file);
            }
        } catch (RemoteException e) {
            System.err.println("Failed to send file list to new client: " + userId);
            fileClients.remove(userId);
        }
    }
    
    @Override
    public void unregisterFileClient(ClientCallback client, String userId) throws RemoteException {
        if (userId != null) {
            fileClients.remove(userId);
            System.out.println("File client unregistered: " + userId + 
                              " (Total file clients: " + fileClients.size() + ")");
        }
    }
    
    /**
     * Generate unique file ID
     */
    private String generateFileId(FileTransfer fileData) {
        return fileData.getUploaderId() + "_" + System.currentTimeMillis() + "_" + 
               Math.abs(fileData.getFileName().hashCode());
    }
    
    /**
     * Assemble chunked file into complete file
     */
    private boolean assembleChunkedFile(FileTransfer lastChunk) throws RemoteException {
        try {
            String fileId = lastChunk.getFileId();
            String tempDir = storageDirectory + File.separator + "temp";
            
            List<byte[]> chunks = new ArrayList<>();
            
            // Read all chunks
            for (int i = 0; i < lastChunk.getTotalChunks(); i++) {
                String chunkPath = tempDir + File.separator + fileId + "_chunk_" + i;
                if (FileUtils.fileExists(chunkPath)) {
                    byte[] chunkData = FileUtils.readFileAsBytes(chunkPath);
                    chunks.add(chunkData);
                } else {
                    throw new RemoteException("Missing chunk: " + i);
                }
            }
            
            // Merge chunks
            byte[] completeFile = FileUtils.mergeFileChunks(chunks);
            
            // Create complete file transfer object
            FileTransfer completeFileTransfer = new FileTransfer(
                lastChunk.getFileName(),
                lastChunk.getFileType(),
                completeFile,
                lastChunk.getUploaderId(),
                lastChunk.getUploaderName()
            );
            completeFileTransfer.setFileId(fileId);
            
            // Upload the complete file
            uploadFile(completeFileTransfer);
            
            // Clean up chunks
            for (int i = 0; i < lastChunk.getTotalChunks(); i++) {
                String chunkPath = tempDir + File.separator + fileId + "_chunk_" + i;
                FileUtils.deleteFile(chunkPath);
            }
            
            System.out.println("Chunked file assembled: " + lastChunk.getFileName());
            
            return true;
            
        } catch (IOException e) {
            throw new RemoteException("Failed to assemble chunked file: " + e.getMessage());
        }
    }
    
    /**
     * Load existing files from storage directory
     */
    private void loadExistingFiles() {
        try {
            File storageDir = new File(storageDirectory);
            if (!storageDir.exists()) {
                return;
            }
            
            File[] files = storageDir.listFiles();
            if (files == null) {
                return;
            }
            
            for (File file : files) {
                if (file.isFile() && !file.getName().startsWith("temp")) {
                    try {
                        String fileName = file.getName();
                        String[] parts = fileName.split("\\.");
                        if (parts.length >= 2) {
                            String fileId = parts[0];
                            String extension = parts[parts.length - 1];
                            
                            FileTransfer metadata = new FileTransfer();
                            metadata.setFileId(fileId);
                            metadata.setFileName(fileName);
                            metadata.setFileType(extension);
                            metadata.setFileSize(file.length());
                            metadata.setUploadTime(file.lastModified());
                            metadata.setUploaderId("system");
                            metadata.setUploaderName("System");
                            
                            storedFiles.put(fileId, metadata);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load existing file: " + file.getName());
                    }
                }
            }
            
            System.out.println("Loaded " + storedFiles.size() + " existing files");
            
        } catch (Exception e) {
            System.err.println("Failed to load existing files: " + e.getMessage());
        }
    }
    
    /**
     * Notify all file clients
     */
    private void notifyAllFileClients(FileClientNotification notification) {
        List<String> disconnectedClients = new ArrayList<>();
        
        for (Map.Entry<String, ClientCallback> entry : fileClients.entrySet()) {
            try {
                notification.notify(entry.getValue());
            } catch (RemoteException e) {
                System.err.println("File client disconnected: " + entry.getKey());
                disconnectedClients.add(entry.getKey());
            }
        }
        
        // Remove disconnected clients
        for (String clientId : disconnectedClients) {
            fileClients.remove(clientId);
        }
    }
    
    /**
     * Functional interface for file client notifications
     */
    @FunctionalInterface
    private interface FileClientNotification {
        void notify(ClientCallback callback) throws RemoteException;
    }
    
    /**
     * Shutdown the file server
     */
    public void shutdown() {
        try {
            // Notify all clients about server shutdown
            notifyAllFileClients(callback -> callback.onServerNotification(
                "File server is shutting down..."));
            
            fileClients.clear();
            System.out.println("FileServer shutdown completed");
            
        } catch (Exception e) {
            System.err.println("Error during FileServer shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Get server statistics
     */
    public String getServerStats() {
        long totalSize = storedFiles.values().stream().mapToLong(FileTransfer::getFileSize).sum();
        return String.format("FileServer Stats - Files: %d, Total Size: %s, Clients: %d", 
                           storedFiles.size(), FileUtils.formatFileSize(totalSize), fileClients.size());
    }
}
