package com.collabboard.interfaces;

import com.collabboard.models.DrawingAction;
import com.collabboard.models.ChatMessage;
import com.collabboard.models.FileTransfer;
import com.collabboard.models.User;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Callback interface for clients to receive updates from the server
 * This interface is implemented by clients to receive real-time notifications
 */
public interface ClientCallback extends Remote {
    
    // Whiteboard callbacks
    /**
     * Called when a new drawing action is received
     * @param action The drawing action that was performed
     * @throws RemoteException if RMI communication fails
     */
    void onDrawingActionReceived(DrawingAction action) throws RemoteException;
    
    /**
     * Called when the whiteboard is cleared
     * @param userId The user who cleared the whiteboard
     * @throws RemoteException if RMI communication fails
     */
    void onWhiteboardCleared(String userId) throws RemoteException;
    
    /**
     * Called when an undo action is performed
     * @param userId The user who performed the undo
     * @throws RemoteException if RMI communication fails
     */
    void onUndoActionReceived(String userId) throws RemoteException;
    
    // Chat callbacks
    /**
     * Called when a new chat message is received
     * @param message The chat message that was received
     * @throws RemoteException if RMI communication fails
     */
    void onChatMessageReceived(ChatMessage message) throws RemoteException;
    
    /**
     * Called when user list is updated
     * @param users List of currently online users
     * @throws RemoteException if RMI communication fails
     */
    void onUserListUpdated(List<User> users) throws RemoteException;
    
    /**
     * Called when a user joins the session
     * @param user The user who joined
     * @throws RemoteException if RMI communication fails
     */
    void onUserJoined(User user) throws RemoteException;
    
    /**
     * Called when a user leaves the session
     * @param user The user who left
     * @throws RemoteException if RMI communication fails
     */
    void onUserLeft(User user) throws RemoteException;
    
    /**
     * Called when a user is typing
     * @param userId The user ID who is typing
     * @param username The username who is typing
     * @param isTyping true if started typing, false if stopped
     * @throws RemoteException if RMI communication fails
     */
    void onUserTyping(String userId, String username, boolean isTyping) throws RemoteException;
    
    // File sharing callbacks
    /**
     * Called when a new file is shared
     * @param fileMetadata The metadata of the shared file
     * @throws RemoteException if RMI communication fails
     */
    void onFileShared(FileTransfer fileMetadata) throws RemoteException;
    
    /**
     * Called when a file is deleted
     * @param fileId The ID of the deleted file
     * @param deletedBy The user who deleted the file
     * @throws RemoteException if RMI communication fails
     */
    void onFileDeleted(String fileId, String deletedBy) throws RemoteException;
    
    // Audio callbacks
    /**
     * Called when audio session starts
     * @param sessionId The audio session ID
     * @throws RemoteException if RMI communication fails
     */
    void onAudioSessionStarted(String sessionId) throws RemoteException;
    
    /**
     * Called when audio session ends
     * @param sessionId The audio session ID
     * @throws RemoteException if RMI communication fails
     */
    void onAudioSessionEnded(String sessionId) throws RemoteException;
    
    /**
     * Called when a user joins audio session
     * @param userId The user ID who joined audio
     * @param username The username who joined audio
     * @throws RemoteException if RMI communication fails
     */
    void onUserJoinedAudio(String userId, String username) throws RemoteException;
    
    /**
     * Called when a user leaves audio session
     * @param userId The user ID who left audio
     * @param username The username who left audio
     * @throws RemoteException if RMI communication fails
     */
    void onUserLeftAudio(String userId, String username) throws RemoteException;
    
    // General callbacks
    /**
     * Called when server sends a notification message
     * @param message The notification message
     * @throws RemoteException if RMI communication fails
     */
    void onServerNotification(String message) throws RemoteException;
    
    /**
     * Called when an error occurs on the server
     * @param errorMessage The error message
     * @throws RemoteException if RMI communication fails
     */
    void onServerError(String errorMessage) throws RemoteException;
}
