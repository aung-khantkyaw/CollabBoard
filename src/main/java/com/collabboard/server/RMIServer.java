package com.collabboard.server;

import com.collabboard.interfaces.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;

/**
 * Main RMI Server class that starts all services and binds them to the registry
 */
public class RMIServer {
    
    private static final String CONFIG_FILE = "src/main/resources/config.properties";
    private Properties config;
    
    private WhiteboardServerImpl whiteboardServer;
    private ChatServerImpl chatServer;
    private FileServerImpl fileServer;
    
    public RMIServer() {
        loadConfiguration();
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
    }
    
    /**
     * Set default configuration values
     */
    private void setDefaultConfiguration() {
        config.setProperty("rmi.registry.port", "1099");
        config.setProperty("service.whiteboard.name", "WhiteboardService");
        config.setProperty("service.chat.name", "ChatService");
        config.setProperty("service.file.name", "FileService");
        config.setProperty("file.storage.directory", "./files");
        config.setProperty("whiteboard.save.directory", "./whiteboards");
    }
    
    /**
     * Start the RMI server and all services
     */
    public void startServer() {
        try {
            // Create RMI registry
            int registryPort = Integer.parseInt(config.getProperty("rmi.registry.port", "1099"));
            Registry registry;
            
            try {
                registry = LocateRegistry.createRegistry(registryPort);
                System.out.println("RMI Registry created on port " + registryPort);
            } catch (Exception e) {
                registry = LocateRegistry.getRegistry(registryPort);
                System.out.println("Using existing RMI Registry on port " + registryPort);
            }
            
            // Create and export service implementations
            createServices();
            
            // Bind services to registry
            bindServices(registry);
            
            System.out.println("CollabBoard RMI Server started successfully!");
            System.out.println("Server is ready and waiting for client connections...");
            
            // Keep server running
            synchronized (this) {
                wait();
            }
            
        } catch (Exception e) {
            System.err.println("Server startup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create service implementations
     */
    private void createServices() throws Exception {
        // Create whiteboard service (already exported via UnicastRemoteObject constructor)
        whiteboardServer = new WhiteboardServerImpl(config);
        
        // Create chat service (already exported via UnicastRemoteObject constructor)
        chatServer = new ChatServerImpl(config);
        
        // Create file service (already exported via UnicastRemoteObject constructor)
        fileServer = new FileServerImpl(config);
        
        System.out.println("All services created successfully");
    }
    
    /**
     * Bind services to RMI registry
     */
    private void bindServices(Registry registry) throws Exception {
        String whiteboardName = config.getProperty("service.whiteboard.name", "WhiteboardService");
        String chatName = config.getProperty("service.chat.name", "ChatService");
        String fileName = config.getProperty("service.file.name", "FileService");
        
        registry.rebind(whiteboardName, whiteboardServer);
        registry.rebind(chatName, chatServer);
        registry.rebind(fileName, fileServer);
        
        System.out.println("Services bound to registry:");
        System.out.println("  - " + whiteboardName);
        System.out.println("  - " + chatName);
        System.out.println("  - " + fileName);
    }
    
    /**
     * Shutdown the server gracefully
     */
    public void shutdown() {
        try {
            if (whiteboardServer != null) {
                whiteboardServer.shutdown();
            }
            if (chatServer != null) {
                chatServer.shutdown();
            }
            if (fileServer != null) {
                fileServer.shutdown();
            }
            
            System.out.println("Server shutdown completed");
            
        } catch (Exception e) {
            System.err.println("Error during server shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Main method to start the server
     */
    public static void main(String[] args) {
        // Set system properties for RMI
        String hostname = "localhost"; // Default
        
        // Check if hostname/IP is provided as argument
        if (args.length > 0 && !args[0].trim().isEmpty()) {
            hostname = args[0].trim();
            System.out.println("Using provided hostname: " + hostname);
        } else {
            // Try to get the actual network IP address (Linux-compatible)
            try {
                // Method 1: Try to get non-loopback network interface
                java.net.NetworkInterface.getNetworkInterfaces().asIterator().forEachRemaining(ni -> {
                    try {
                        if (!ni.isLoopback() && ni.isUp()) {
                            ni.getInetAddresses().asIterator().forEachRemaining(addr -> {
                                if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress() && 
                                    addr instanceof java.net.Inet4Address) {
                                    String ip = addr.getHostAddress();
                                    System.setProperty("detected.ip", ip);
                                }
                            });
                        }
                    } catch (Exception e) {
                        // Continue to next interface
                    }
                });
                
                String detectedIP = System.getProperty("detected.ip");
                if (detectedIP != null && !detectedIP.isEmpty()) {
                    hostname = detectedIP;
                    System.out.println("Auto-detected network IP: " + hostname);
                } else {
                    // Fallback: Use InetAddress.getLocalHost()
                    java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
                    String localIP = localHost.getHostAddress();
                    if (!localIP.equals("127.0.0.1") && !localIP.equals("127.0.1.1")) {
                        hostname = localIP;
                        System.out.println("Auto-detected hostname: " + hostname);
                    } else {
                        System.out.println("Warning: Only loopback address detected. Consider providing IP as argument.");
                        System.out.println("Usage: java RMIServer <your-network-ip>");
                        System.out.println("Using default hostname: " + hostname);
                    }
                }
            } catch (Exception e) {
                System.out.println("Could not detect IP, using default hostname: " + hostname);
                System.out.println("Error: " + e.getMessage());
            }
        }
        
        System.setProperty("java.rmi.server.hostname", hostname);
        System.setProperty("java.rmi.server.useLocalHostname", "false");
        System.out.println("RMI server hostname set to: " + hostname);
        
        // Create and start server
        RMIServer server = new RMIServer();
        
        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            server.shutdown();
        }));
        
        // Start the server
        server.startServer();
    }
}
