package com.collabboard.interfaces;

import com.collabboard.models.FileTransfer;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * RMI interface for file sharing operations
 * This interface defines all remote methods for file transfer functionality
 */
public interface FileService extends Remote {
    
    /**
     * Upload a file to the server
     * @param fileData The file transfer object containing file data
     * @return The file ID if upload successful, null otherwise
     * @throws RemoteException if RMI communication fails
     */
    String uploadFile(FileTransfer fileData) throws RemoteException;
    
    /**
     * Download a file from the server
     * @param fileId The ID of the file to download
     * @return FileTransfer object containing file data, null if not found
     * @throws RemoteException if RMI communication fails
     */
    FileTransfer downloadFile(String fileId) throws RemoteException;
    
    /**
     * Get list of all shared files
     * @return List of file transfer objects (metadata only, no data)
     * @throws RemoteException if RMI communication fails
     */
    List<FileTransfer> getSharedFiles() throws RemoteException;
    
    /**
     * Share a file with all users (notify them about new file)
     * @param fileId The ID of the file to share
     * @param uploaderId The user ID of the file uploader
     * @throws RemoteException if RMI communication fails
     */
    void shareFile(String fileId, String uploaderId) throws RemoteException;
    
    /**
     * Delete a file from the server
     * @param fileId The ID of the file to delete
     * @param userId The user ID requesting deletion
     * @return true if deleted successfully, false otherwise
     * @throws RemoteException if RMI communication fails
     */
    boolean deleteFile(String fileId, String userId) throws RemoteException;
    
    /**
     * Upload a file chunk (for large files)
     * @param fileChunk The file chunk data
     * @return true if chunk uploaded successfully, false otherwise
     * @throws RemoteException if RMI communication fails
     */
    boolean uploadFileChunk(FileTransfer fileChunk) throws RemoteException;
    
    /**
     * Check if a file exists on the server
     * @param fileId The ID of the file to check
     * @return true if file exists, false otherwise
     * @throws RemoteException if RMI communication fails
     */
    boolean fileExists(String fileId) throws RemoteException;
    
    /**
     * Get file metadata without downloading the actual file
     * @param fileId The ID of the file
     * @return FileTransfer object with metadata only (no data)
     * @throws RemoteException if RMI communication fails
     */
    FileTransfer getFileMetadata(String fileId) throws RemoteException;
    
    /**
     * Register a client for receiving file sharing notifications
     * @param client The client callback interface
     * @param userId The user ID
     * @throws RemoteException if RMI communication fails
     */
    void registerFileClient(ClientCallback client, String userId) throws RemoteException;
    
    /**
     * Unregister a client from receiving file sharing notifications
     * @param client The client callback interface
     * @param userId The user ID
     * @throws RemoteException if RMI communication fails
     */
    void unregisterFileClient(ClientCallback client, String userId) throws RemoteException;
}
