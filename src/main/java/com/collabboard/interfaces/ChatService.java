package com.collabboard.interfaces;

import com.collabboard.models.ChatMessage;
import com.collabboard.models.User;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * RMI interface for chat operations
 * This interface defines all remote methods for chat functionality
 */
public interface ChatService extends Remote {
    
    /**
     * Send a chat message to all connected clients
     * @param message The chat message to send
     * @throws RemoteException if RMI communication fails
     */
    void sendMessage(ChatMessage message) throws RemoteException;
    
    /**
     * Get chat message history
     * @param limit Maximum number of messages to retrieve
     * @return List of recent chat messages
     * @throws RemoteException if RMI communication fails
     */
    List<ChatMessage> getMessageHistory(int limit) throws RemoteException;
    
    /**
     * Register a client for receiving chat updates
     * @param client The client callback interface
     * @param user The user information
     * @throws RemoteException if RMI communication fails
     */
    void registerChatClient(ClientCallback client, User user) throws RemoteException;
    
    /**
     * Unregister a client from receiving chat updates
     * @param client The client callback interface
     * @param userId The user ID of the client
     * @throws RemoteException if RMI communication fails
     */
    void unregisterChatClient(ClientCallback client, String userId) throws RemoteException;
    
    /**
     * Get list of currently online users
     * @return List of online users
     * @throws RemoteException if RMI communication fails
     */
    List<User> getOnlineUsers() throws RemoteException;
    
    /**
     * Update user status (online/offline, audio enabled, etc.)
     * @param userId The user ID
     * @param user Updated user information
     * @throws RemoteException if RMI communication fails
     */
    void updateUserStatus(String userId, User user) throws RemoteException;
    
    /**
     * Notify that a user is typing
     * @param userId The user ID who is typing
     * @param username The username who is typing
     * @param isTyping true if user started typing, false if stopped
     * @throws RemoteException if RMI communication fails
     */
    void notifyTyping(String userId, String username, boolean isTyping) throws RemoteException;
    
    /**
     * Get the total number of messages in the chat
     * @return Total message count
     * @throws RemoteException if RMI communication fails
     */
    int getTotalMessageCount() throws RemoteException;
}
