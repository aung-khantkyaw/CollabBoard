package com.collabboard.server;

import com.collabboard.interfaces.WhiteboardService;
import com.collabboard.interfaces.ClientCallback;
import com.collabboard.models.DrawingAction;
import com.collabboard.utils.FileUtils;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.*;

/**
 * Server implementation of the WhiteboardService interface
 */
public class WhiteboardServerImpl extends UnicastRemoteObject implements WhiteboardService {
    
    private final List<DrawingAction> drawingActions;
    private final Map<String, ClientCallback> clients;
    private final Properties config;
    private final String saveDirectory;
    private final int maxActions;
    
    public WhiteboardServerImpl(Properties config) throws RemoteException {
        super();
        this.config = config;
        this.drawingActions = new CopyOnWriteArrayList<>();
        this.clients = new ConcurrentHashMap<>();
        this.saveDirectory = config.getProperty("whiteboard.save.directory", "./whiteboards");
        this.maxActions = Integer.parseInt(config.getProperty("whiteboard.max.actions", "10000"));
        
        // Create save directory if it doesn't exist
        FileUtils.createDirectoryIfNotExists(saveDirectory);
        
        System.out.println("WhiteboardServer initialized");
    }
    
    @Override
    public synchronized void addDrawingAction(DrawingAction action) throws RemoteException {
        if (action == null) {
            throw new RemoteException("Drawing action cannot be null");
        }
        
        // Add timestamp if not set
        if (action.getTimestamp() == 0) {
            action.setTimestamp(System.currentTimeMillis());
        }
        
        // Add action to list
        drawingActions.add(action);
        
        // Limit the number of actions to prevent memory issues
        if (drawingActions.size() > maxActions) {
            drawingActions.remove(0);
        }
        
        System.out.println("Drawing action added: " + action.getActionType() + " by " + action.getUserId());
        
        // Notify all clients about the new action
        notifyAllClients(callback -> callback.onDrawingActionReceived(action));
    }
    
    @Override
    public synchronized void clearWhiteboard(String userId) throws RemoteException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new RemoteException("User ID cannot be null or empty");
        }
        
        drawingActions.clear();
        
        System.out.println("Whiteboard cleared by user: " + userId);
        
        // Notify all clients about the clear action
        notifyAllClients(callback -> callback.onWhiteboardCleared(userId));
    }
    
    @Override
    public synchronized void undoLastAction(String userId) throws RemoteException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new RemoteException("User ID cannot be null or empty");
        }
        
        if (!drawingActions.isEmpty()) {
            DrawingAction lastAction = drawingActions.remove(drawingActions.size() - 1);
            System.out.println("Undo action performed by user: " + userId + 
                             ", removed action: " + lastAction.getActionType());
        }
        
        // Notify all clients about the undo action
        notifyAllClients(callback -> callback.onUndoActionReceived(userId));
    }
    
    @Override
    public List<DrawingAction> getAllActions() throws RemoteException {
        return new ArrayList<>(drawingActions);
    }
    
    @Override
    public void registerClient(ClientCallback client, String userId) throws RemoteException {
        if (client == null) {
            throw new RemoteException("Client callback cannot be null");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new RemoteException("User ID cannot be null or empty");
        }
        
        clients.put(userId, client);
        System.out.println("Client registered for whiteboard updates: " + userId + 
                          " (Total clients: " + clients.size() + ")");
        
        // Send current whiteboard state to the new client
        try {
            for (DrawingAction action : drawingActions) {
                client.onDrawingActionReceived(action);
            }
        } catch (RemoteException e) {
            System.err.println("Failed to send current state to new client: " + userId);
            clients.remove(userId);
        }
    }
    
    @Override
    public void unregisterClient(ClientCallback client, String userId) throws RemoteException {
        if (userId != null) {
            clients.remove(userId);
            System.out.println("Client unregistered from whiteboard updates: " + userId + 
                              " (Total clients: " + clients.size() + ")");
        }
    }
    
    @Override
    public int getClientCount() throws RemoteException {
        return clients.size();
    }
    
    @Override
    public boolean saveWhiteboard(String fileName, String userId) throws RemoteException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new RemoteException("File name cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new RemoteException("User ID cannot be null or empty");
        }
        
        try {
            String safeFileName = FileUtils.getSafeFileName(fileName);
            if (!safeFileName.endsWith(".wb")) {
                safeFileName += ".wb";
            }
            
            String filePath = saveDirectory + File.separator + safeFileName;
            
            // Serialize drawing actions to file
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
                oos.writeObject(new ArrayList<>(drawingActions));
                oos.writeObject(System.currentTimeMillis()); // Save timestamp
                oos.writeObject(userId); // Save who saved it
            }
            
            System.out.println("Whiteboard saved to: " + filePath + " by user: " + userId);
            
            // Notify clients about successful save
            notifyAllClients(callback -> callback.onServerNotification(
                "Whiteboard saved as '" + fileName + "' by " + userId));
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Failed to save whiteboard: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean loadWhiteboard(String fileName, String userId) throws RemoteException {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new RemoteException("File name cannot be null or empty");
        }
        if (userId == null || userId.trim().isEmpty()) {
            throw new RemoteException("User ID cannot be null or empty");
        }
        
        try {
            String safeFileName = FileUtils.getSafeFileName(fileName);
            if (!safeFileName.endsWith(".wb")) {
                safeFileName += ".wb";
            }
            
            String filePath = saveDirectory + File.separator + safeFileName;
            
            if (!FileUtils.fileExists(filePath)) {
                System.err.println("Whiteboard file not found: " + filePath);
                return false;
            }
            
            // Deserialize drawing actions from file
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
                @SuppressWarnings("unchecked")
                List<DrawingAction> loadedActions = (List<DrawingAction>) ois.readObject();
                
                // Clear current actions and load from file
                synchronized (this) {
                    drawingActions.clear();
                    drawingActions.addAll(loadedActions);
                }
                
                System.out.println("Whiteboard loaded from: " + filePath + " by user: " + userId + 
                                  " (Actions: " + loadedActions.size() + ")");
                
                // Notify all clients about the cleared whiteboard first
                notifyAllClients(callback -> callback.onWhiteboardCleared(userId));
                
                // Then send all loaded actions
                for (DrawingAction action : loadedActions) {
                    notifyAllClients(callback -> callback.onDrawingActionReceived(action));
                }
                
                // Notify about successful load
                notifyAllClients(callback -> callback.onServerNotification(
                    "Whiteboard '" + fileName + "' loaded by " + userId));
                
                return true;
            }
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load whiteboard: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Notify all connected clients
     */
    private void notifyAllClients(ClientNotification notification) {
        List<String> disconnectedClients = new ArrayList<>();
        
        for (Map.Entry<String, ClientCallback> entry : clients.entrySet()) {
            try {
                notification.notify(entry.getValue());
            } catch (RemoteException e) {
                System.err.println("Client disconnected: " + entry.getKey());
                disconnectedClients.add(entry.getKey());
            }
        }
        
        // Remove disconnected clients
        for (String clientId : disconnectedClients) {
            clients.remove(clientId);
        }
    }
    
    /**
     * Functional interface for client notifications
     */
    @FunctionalInterface
    private interface ClientNotification {
        void notify(ClientCallback callback) throws RemoteException;
    }
    
    /**
     * Shutdown the whiteboard server
     */
    public void shutdown() {
        try {
            // Notify all clients about server shutdown
            notifyAllClients(callback -> callback.onServerNotification(
                "Server is shutting down..."));
            
            clients.clear();
            System.out.println("WhiteboardServer shutdown completed");
            
        } catch (Exception e) {
            System.err.println("Error during WhiteboardServer shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Get server statistics
     */
    public String getServerStats() {
        return String.format("WhiteboardServer Stats - Actions: %d, Clients: %d", 
                           drawingActions.size(), clients.size());
    }
}
