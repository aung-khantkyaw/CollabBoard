package com.collabboard.client;

import com.collabboard.interfaces.*;
import com.collabboard.models.*;
import com.collabboard.gui.MainWindow;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

/**
 * Main RMI Client class that connects to server and manages all client services
 */
public class RMIClient implements ClientCallback {
    
    private static final String CONFIG_FILE = "src/main/resources/config.properties";
    
    private Properties config;
    private String serverHost;
    private int serverPort;
    
    // RMI services
    private WhiteboardService whiteboardService;
    private ChatService chatService;
    private FileService fileService;
    
    // Client information
    private User currentUser;
    private String userId;
    private String username;
    
    // GUI
    private MainWindow mainWindow;
    
    // Connection state
    private boolean connected = false;
    
    public RMIClient(String serverHost) {
        this.serverHost = serverHost != null ? serverHost : "localhost";
        loadConfiguration();
        setupUser();
    }
    
    /**
     * Load configuration from properties file
     */
    private void loadConfiguration() {
        config = new Properties();
        try {
            config.load(new FileInputStream(CONFIG_FILE));
        } catch (IOException e) {
            System.err.println("Could not load configuration file, using defaults");
            setDefaultConfiguration();
        }
        
        this.serverPort = Integer.parseInt(config.getProperty("rmi.registry.port", "1099"));
    }
    
    /**
     * Set default configuration values
     */
    private void setDefaultConfiguration() {
        config.setProperty("rmi.registry.port", "1099");
        config.setProperty("service.whiteboard.name", "WhiteboardService");
        config.setProperty("service.chat.name", "ChatService");
        config.setProperty("service.file.name", "FileService");
    }
    
    /**
     * Setup user information
     */
    private void setupUser() {
        // Generate unique user ID
        this.userId = "user_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
        
        // Get username from user
        this.username = JOptionPane.showInputDialog(null, 
            "Enter your username:", "CollabBoard Login", JOptionPane.QUESTION_MESSAGE);
        
        if (username == null || username.trim().isEmpty()) {
            this.username = "User_" + userId.substring(userId.length() - 4);
        }
        
        // Create user object
        this.currentUser = new User(userId, username, "localhost");
        
        System.out.println("User setup: " + username + " (" + userId + ")");
    }
    
    /**
     * Connect to the RMI server
     */
    public boolean connectToServer() {
        try {
            System.out.println("Connecting to server at " + serverHost + ":" + serverPort);
            
            // Get RMI registry
            Registry registry = LocateRegistry.getRegistry(serverHost, serverPort);
            
            // Lookup services
            String whiteboardName = config.getProperty("service.whiteboard.name", "WhiteboardService");
            String chatName = config.getProperty("service.chat.name", "ChatService");
            String fileName = config.getProperty("service.file.name", "FileService");
            
            whiteboardService = (WhiteboardService) registry.lookup(whiteboardName);
            chatService = (ChatService) registry.lookup(chatName);
            fileService = (FileService) registry.lookup(fileName);
            
            // Export this client for callbacks
            ClientCallback clientStub = (ClientCallback) UnicastRemoteObject.exportObject(this, 0);
            
            // Register with services
            whiteboardService.registerClient(clientStub, userId);
            chatService.registerChatClient(clientStub, currentUser);
            fileService.registerFileClient(clientStub, userId);
            
            connected = true;
            System.out.println("Successfully connected to server");
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            e.printStackTrace();
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, 
                    "Failed to connect to server at " + serverHost + ":" + serverPort + 
                    "\n\nError: " + e.getMessage() + 
                    "\n\nPlease ensure the server is running and try again.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            });
            
            return false;
        }
    }
    
    /**
     * Disconnect from the server
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        
        try {
            // Unregister from services
            if (whiteboardService != null) {
                whiteboardService.unregisterClient(this, userId);
            }
            if (chatService != null) {
                chatService.unregisterChatClient(this, userId);
            }
            if (fileService != null) {
                fileService.unregisterFileClient(this, userId);
            }
            
            // Unexport this client
            UnicastRemoteObject.unexportObject(this, true);
            
            connected = false;
            System.out.println("Disconnected from server");
            
        } catch (Exception e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }
    
    /**
     * Start the client GUI
     */
    public void startGUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                mainWindow = new MainWindow(this);
                mainWindow.setVisible(true);
                
                // Initialize with current user list after GUI is ready
                if (connected && chatService != null) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            List<User> currentUsers = chatService.getOnlineUsers();
                            mainWindow.getUserListPanel().updateUserList(currentUsers);
                        } catch (Exception e) {
                            System.err.println("Failed to get initial user list: " + e.getMessage());
                        }
                    });
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Failed to start GUI: " + e.getMessage(),
                    "GUI Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    // Getter methods for GUI to access services
    public WhiteboardService getWhiteboardService() { return whiteboardService; }
    public ChatService getChatService() { return chatService; }
    public FileService getFileService() { return fileService; }
    public User getCurrentUser() { return currentUser; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public boolean isConnected() { return connected; }
    
    // =============================================================================
    // ClientCallback Implementation
    // =============================================================================
    
    @Override
    public void onDrawingActionReceived(DrawingAction action) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getWhiteboardPanel().addDrawingAction(action);
            }
        });
    }
    
    @Override
    public void onWhiteboardCleared(String userId) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getWhiteboardPanel().clearWhiteboardLocal();
            }
        });
    }
    
    @Override
    public void onUndoActionReceived(String userId) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getWhiteboardPanel().undoLastAction();
            }
        });
    }
    
    @Override
    public void onChatMessageReceived(ChatMessage message) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getChatPanel().addMessage(message);
            }
        });
    }
    
    @Override
    public void onUserListUpdated(java.util.List<User> users) throws RemoteException {
        System.out.println("RMIClient: Received user list update with " + users.size() + " users");
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getUserListPanel().updateUserList(users);
            }
        });
    }
    
    @Override
    public void onUserJoined(User user) throws RemoteException {
        System.out.println("RMIClient: User joined - " + user.getUsername());
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getChatPanel().addSystemMessage(user.getUsername() + " joined the session");
                mainWindow.getUserListPanel().addUser(user);
            }
        });
    }
    
    @Override
    public void onUserLeft(User user) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getChatPanel().addSystemMessage(user.getUsername() + " left the session");
                mainWindow.getUserListPanel().removeUser(user);
            }
        });
    }
    
    @Override
    public void onUserTyping(String userId, String username, boolean isTyping) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getChatPanel().updateTypingStatus(userId, username, isTyping);
            }
        });
    }
    
    @Override
    public void onFileShared(FileTransfer fileMetadata) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getFileSharePanel().addSharedFile(fileMetadata);
                mainWindow.getChatPanel().addSystemMessage(
                    fileMetadata.getUploaderName() + " shared file: " + fileMetadata.getFileName());
            }
        });
    }
    
    @Override
    public void onFileDeleted(String fileId, String deletedBy) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getFileSharePanel().removeSharedFile(fileId);
                mainWindow.getChatPanel().addSystemMessage("File deleted by " + deletedBy);
            }
        });
    }
    
    @Override
    public void onAudioSessionStarted(String sessionId) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getAudioControlPanel().onAudioSessionStarted(sessionId);
                mainWindow.getChatPanel().addSystemMessage("Audio session started");
            }
        });
    }
    
    @Override
    public void onAudioSessionEnded(String sessionId) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getAudioControlPanel().onAudioSessionEnded(sessionId);
                mainWindow.getChatPanel().addSystemMessage("Audio session ended");
            }
        });
    }
    
    @Override
    public void onUserJoinedAudio(String userId, String username) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getAudioControlPanel().onUserJoinedAudio(userId, username);
                mainWindow.getChatPanel().addSystemMessage(username + " joined audio");
            }
        });
    }
    
    @Override
    public void onUserLeftAudio(String userId, String username) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getAudioControlPanel().onUserLeftAudio(userId, username);
                mainWindow.getChatPanel().addSystemMessage(username + " left audio");
            }
        });
    }
    
    @Override
    public void onServerNotification(String message) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                mainWindow.getChatPanel().addSystemMessage("Server: " + message);
            }
            System.out.println("Server notification: " + message);
        });
    }
    
    @Override
    public void onServerError(String errorMessage) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(mainWindow, 
                "Server Error: " + errorMessage,
                "Server Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    /**
     * Main method to start the client
     */
    public static void main(String[] args) {
        // Set system properties
        System.setProperty("java.awt.headless", "false");
        
        // Get server host from command line or system property
        String serverHost = null;
        if (args.length > 0) {
            serverHost = args[0];
        } else {
            serverHost = System.getProperty("server.host");
        }
        
        // Create and start client
        RMIClient client = new RMIClient(serverHost);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down client...");
            client.disconnect();
        }));
        
        // Connect to server
        if (client.connectToServer()) {
            // Start GUI
            client.startGUI();
        } else {
            System.exit(1);
        }
    }
}
