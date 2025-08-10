package com.collabboard.server;

import com.collabboard.models.ChatMessage;
import com.collabboard.models.User;
import com.collabboard.interfaces.ChatService;
import com.collabboard.interfaces.ClientCallback;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Implementation of the ChatService interface for handling chat messages
 * and user management in the distributed whiteboard application.
 */
public class ChatServerImpl extends UnicastRemoteObject implements ChatService {
    
    private List<ChatMessage> messages;
    private Map<String, ClientCallback> clients; // userId -> ClientCallback
    private Map<String, User> users; // userId -> User
    private Properties config;
    
    public ChatServerImpl() throws RemoteException {
        super();
        this.messages = new CopyOnWriteArrayList<>();
        this.clients = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        System.out.println("ChatServerImpl initialized");
    }
    
    public ChatServerImpl(Properties config) throws RemoteException {
        super();
        this.config = config;
        this.messages = new CopyOnWriteArrayList<>();
        this.clients = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        System.out.println("ChatServerImpl initialized with configuration");
    }
    
    @Override
    public synchronized void sendMessage(ChatMessage message) throws RemoteException {
        System.out.println("Received message: " + message.getContent() + " from " + message.getUsername());
        
        // Add message to history
        messages.add(message);
        
        // Notify all clients about the new message
        List<String> clientsToRemove = new ArrayList<>();
        for (Map.Entry<String, ClientCallback> entry : clients.entrySet()) {
            try {
                entry.getValue().onChatMessageReceived(message);
            } catch (RemoteException e) {
                System.err.println("Failed to notify client of message: " + e.getMessage());
                clientsToRemove.add(entry.getKey());
            }
        }
        
        // Remove disconnected clients
        for (String userId : clientsToRemove) {
            clients.remove(userId);
            users.remove(userId);
        }
    }
    
    @Override
    public synchronized List<ChatMessage> getMessageHistory(int limit) throws RemoteException {
        if (limit <= 0 || limit > messages.size()) {
            return new ArrayList<>(messages);
        }
        
        int startIndex = Math.max(0, messages.size() - limit);
        return new ArrayList<>(messages.subList(startIndex, messages.size()));
    }
    
    @Override
    public synchronized void registerChatClient(ClientCallback client, User user) throws RemoteException {
        clients.put(user.getUserId(), client);
        users.put(user.getUserId(), user);
        
        System.out.println("User registered: " + user.getUsername() + ". Total users: " + users.size());
        
        // Notify all clients about the updated user list
        notifyUserListUpdate();
    }
    
    @Override
    public synchronized void unregisterChatClient(ClientCallback client, String userId) throws RemoteException {
        clients.remove(userId);
        User removedUser = users.remove(userId);
        
        if (removedUser != null) {
            System.out.println("User unregistered: " + removedUser.getUsername() + ". Total users: " + users.size());
            
            // Notify all clients about the updated user list
            notifyUserListUpdate();
        }
    }
    
    @Override
    public synchronized List<User> getOnlineUsers() throws RemoteException {
        return new ArrayList<>(users.values());
    }
    
    @Override
    public synchronized void updateUserStatus(String userId, User user) throws RemoteException {
        if (users.containsKey(userId)) {
            users.put(userId, user);
            System.out.println("Updated user status for: " + user.getUsername());
            
            // Notify all clients about the user status update
            notifyUserListUpdate();
        }
    }
    
    @Override
    public synchronized void notifyTyping(String userId, String username, boolean isTyping) throws RemoteException {
        System.out.println("User " + username + " typing status: " + isTyping);
        
        // Notify all other clients about typing status
        List<String> clientsToRemove = new ArrayList<>();
        for (Map.Entry<String, ClientCallback> entry : clients.entrySet()) {
            if (!entry.getKey().equals(userId)) { // Don't notify the typing user
                try {
                    entry.getValue().onUserTyping(userId, username, isTyping);
                } catch (RemoteException e) {
                    System.err.println("Failed to notify client of typing status: " + e.getMessage());
                    clientsToRemove.add(entry.getKey());
                }
            }
        }
        
        // Remove disconnected clients
        for (String clientId : clientsToRemove) {
            clients.remove(clientId);
            users.remove(clientId);
        }
    }
    
    @Override
    public synchronized int getTotalMessageCount() throws RemoteException {
        return messages.size();
    }
    
    /**
     * Notifies all registered clients about user list updates
     */
    private void notifyUserListUpdate() {
        List<String> clientsToRemove = new ArrayList<>();
        List<User> userList = new ArrayList<>(users.values());
        
        for (Map.Entry<String, ClientCallback> entry : clients.entrySet()) {
            try {
                entry.getValue().onUserListUpdated(userList);
            } catch (RemoteException e) {
                System.err.println("Failed to notify client of user list update: " + e.getMessage());
                clientsToRemove.add(entry.getKey());
            }
        }
        
        // Remove disconnected clients
        for (String userId : clientsToRemove) {
            clients.remove(userId);
            users.remove(userId);
            System.out.println("Removed disconnected client from chat notifications");
        }
    }
    
    /**
     * Gets the current number of registered clients
     * @return number of clients
     */
    public int getClientCount() {
        return clients.size();
    }
    
    /**
     * Gets the current number of users
     * @return number of users
     */
    public int getUserCount() {
        return users.size();
    }
    
    /**
     * Clears all messages and users (for testing/reset purposes)
     */
    public synchronized void reset() throws RemoteException {
        messages.clear();
        users.clear();
        
        // Notify all clients about the reset
        List<String> clientsToRemove = new ArrayList<>();
        for (Map.Entry<String, ClientCallback> entry : clients.entrySet()) {
            try {
                entry.getValue().onUserListUpdated(new ArrayList<>());
            } catch (RemoteException e) {
                System.err.println("Failed to notify client of reset: " + e.getMessage());
                clientsToRemove.add(entry.getKey());
            }
        }
        
        // Remove disconnected clients
        for (String userId : clientsToRemove) {
            clients.remove(userId);
        }
        
        System.out.println("Chat server reset completed");
    }
    
    /**
     * Broadcasts a system message to all clients
     * @param systemMessage the system message to broadcast
     */
    public synchronized void broadcastSystemMessage(String systemMessage) throws RemoteException {
        ChatMessage message = new ChatMessage("SYSTEM", "SYSTEM", systemMessage);
        message.setMessageType(ChatMessage.MessageType.SYSTEM);
        sendMessage(message);
    }
    
    /**
     * Checks if a user is currently online
     * @param userId the user ID to check
     * @return true if user exists, false otherwise
     */
    public synchronized boolean isUserOnline(String userId) {
        return users.containsKey(userId);
    }
    
    /**
     * Gets detailed server status information
     * @return status string with client and user counts
     */
    public String getServerStatus() {
        return String.format("ChatServer Status - Clients: %d, Users: %d, Messages: %d", 
                           clients.size(), users.size(), messages.size());
    }
    
    /**
     * Shutdown the chat server gracefully
     */
    public void shutdown() throws RemoteException {
        try {
            // Notify all clients about server shutdown
            broadcastSystemMessage("Server is shutting down...");
            
            // Clear all data structures
            clients.clear();
            users.clear();
            messages.clear();
            
            System.out.println("ChatServer shutdown completed");
        } catch (Exception e) {
            System.err.println("Error during ChatServer shutdown: " + e.getMessage());
        }
    }
}
