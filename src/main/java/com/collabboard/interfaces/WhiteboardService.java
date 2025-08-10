package com.collabboard.interfaces;

import com.collabboard.models.DrawingAction;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * RMI interface for whiteboard operations
 * This interface defines all remote methods for whiteboard functionality
 */
public interface WhiteboardService extends Remote {
    
    /**
     * Add a new drawing action to the whiteboard
     * @param action The drawing action to add
     * @throws RemoteException if RMI communication fails
     */
    void addDrawingAction(DrawingAction action) throws RemoteException;
    
    /**
     * Clear the entire whiteboard
     * @param userId The user requesting the clear operation
     * @throws RemoteException if RMI communication fails
     */
    void clearWhiteboard(String userId) throws RemoteException;
    
    /**
     * Undo the last drawing action
     * @param userId The user requesting the undo operation
     * @throws RemoteException if RMI communication fails
     */
    void undoLastAction(String userId) throws RemoteException;
    
    /**
     * Get all drawing actions on the whiteboard
     * @return List of all drawing actions
     * @throws RemoteException if RMI communication fails
     */
    List<DrawingAction> getAllActions() throws RemoteException;
    
    /**
     * Register a client for receiving whiteboard updates
     * @param client The client callback interface
     * @param userId The user ID of the client
     * @throws RemoteException if RMI communication fails
     */
    void registerClient(ClientCallback client, String userId) throws RemoteException;
    
    /**
     * Unregister a client from receiving whiteboard updates
     * @param client The client callback interface
     * @param userId The user ID of the client
     * @throws RemoteException if RMI communication fails
     */
    void unregisterClient(ClientCallback client, String userId) throws RemoteException;
    
    /**
     * Get the current number of connected clients
     * @return Number of connected clients
     * @throws RemoteException if RMI communication fails
     */
    int getClientCount() throws RemoteException;
    
    /**
     * Save the current whiteboard state
     * @param fileName The name of the file to save
     * @param userId The user requesting the save
     * @return true if saved successfully, false otherwise
     * @throws RemoteException if RMI communication fails
     */
    boolean saveWhiteboard(String fileName, String userId) throws RemoteException;
    
    /**
     * Load a previously saved whiteboard state
     * @param fileName The name of the file to load
     * @param userId The user requesting the load
     * @return true if loaded successfully, false otherwise
     * @throws RemoteException if RMI communication fails
     */
    boolean loadWhiteboard(String fileName, String userId) throws RemoteException;
}
