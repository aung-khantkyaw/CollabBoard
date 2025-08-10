package com.collabboard.gui;

import com.collabboard.client.RMIClient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Main window of the CollabBoard application
 */
public class MainWindow extends JFrame {
    
    private RMIClient client;
    
    // GUI Components
    private WhiteboardPanel whiteboardPanel;
    private ChatPanel chatPanel;
    private ToolbarPanel toolbarPanel;
    private UserListPanel userListPanel;
    private FileSharePanel fileSharePanel;
    private AudioControlPanel audioControlPanel;
    
    // Layout components
    private JPanel mainPanel;
    private JSplitPane mainSplitPane;
    private JSplitPane rightSplitPane;
    private JTabbedPane rightTabbedPane;
    
    public MainWindow(RMIClient client) {
        this.client = client;
        initializeGUI();
        setupWindowProperties();
        setupEventHandlers();
    }
    
    /**
     * Initialize the GUI components
     */
    private void initializeGUI() {
        setTitle("CollabBoard - " + client.getUsername());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main panel
        mainPanel = new JPanel(new BorderLayout());
        
        // Create toolbar
        toolbarPanel = new ToolbarPanel(client);
        mainPanel.add(toolbarPanel, BorderLayout.NORTH);
        
        // Create whiteboard panel
        whiteboardPanel = new WhiteboardPanel(client);
        
        // Connect toolbar to whiteboard panel
        toolbarPanel.setWhiteboardPanel(whiteboardPanel);
        
        // Create chat panel
        chatPanel = new ChatPanel(client);
        
        // Create user list panel
        userListPanel = new UserListPanel(client);
        
        // Create file share panel
        fileSharePanel = new FileSharePanel(client);
        
        // Create audio control panel
        audioControlPanel = new AudioControlPanel(client);
        
        // Create right tabbed pane
        rightTabbedPane = new JTabbedPane();
        rightTabbedPane.addTab("Chat", createChatTab());
        rightTabbedPane.addTab("Users", userListPanel);
        rightTabbedPane.addTab("Files", fileSharePanel);
        rightTabbedPane.addTab("Audio", audioControlPanel);
        
        // Create split panes
        rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightTabbedPane, null);
        rightSplitPane.setDividerLocation(400);
        rightSplitPane.setResizeWeight(1.0);
        
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, whiteboardPanel, rightSplitPane);
        mainSplitPane.setDividerLocation(800);
        mainSplitPane.setResizeWeight(0.75);
        
        mainPanel.add(mainSplitPane, BorderLayout.CENTER);
        
        // Create status bar
        JPanel statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * Create the chat tab with chat panel and user info
     */
    private JPanel createChatTab() {
        JPanel chatTab = new JPanel(new BorderLayout());
        chatTab.add(chatPanel, BorderLayout.CENTER);
        
        // Add connection status
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel connectionStatus = new JLabel("Connected to: " + client.getUsername());
        connectionStatus.setFont(connectionStatus.getFont().deriveFont(Font.PLAIN, 10f));
        connectionStatus.setForeground(Color.DARK_GRAY);
        connectionPanel.add(connectionStatus);
        
        chatTab.add(connectionPanel, BorderLayout.SOUTH);
        
        return chatTab;
    }
    
    /**
     * Create status bar
     */
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JLabel statusLabel = new JLabel("Ready - Connected as " + client.getUsername());
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11f));
        statusBar.add(statusLabel);
        
        return statusBar;
    }
    
    /**
     * Setup window properties
     */
    private void setupWindowProperties() {
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        
        // Set application icon (if available)
        try {
            // You can add an icon here if you have one
            // setIconImage(ImageIO.read(getClass().getResourceAsStream("/icons/app_icon.png")));
        } catch (Exception e) {
            // Ignore if icon not found
        }
        
        // Look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClosing();
            }
        });
        
        // Menu shortcuts
        setupMenuShortcuts();
    }
    
    /**
     * Setup keyboard shortcuts
     */
    private void setupMenuShortcuts() {
        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem saveWhiteboard = new JMenuItem("Save Whiteboard");
        saveWhiteboard.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        saveWhiteboard.addActionListener(e -> whiteboardPanel.saveWhiteboard());
        
        JMenuItem loadWhiteboard = new JMenuItem("Load Whiteboard");
        loadWhiteboard.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        loadWhiteboard.addActionListener(e -> whiteboardPanel.loadWhiteboard());
        
        JMenuItem exit = new JMenuItem("Exit");
        exit.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));
        exit.addActionListener(e -> handleWindowClosing());
        
        fileMenu.add(saveWhiteboard);
        fileMenu.add(loadWhiteboard);
        fileMenu.addSeparator();
        fileMenu.add(exit);
        
        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        
        JMenuItem undo = new JMenuItem("Undo");
        undo.setAccelerator(KeyStroke.getKeyStroke("ctrl Z"));
        undo.addActionListener(e -> whiteboardPanel.undo());
        
        JMenuItem clear = new JMenuItem("Clear Whiteboard");
        clear.setAccelerator(KeyStroke.getKeyStroke("ctrl shift C"));
        clear.addActionListener(e -> whiteboardPanel.clearWhiteboard());
        
        editMenu.add(undo);
        editMenu.addSeparator();
        editMenu.add(clear);
        
        // View menu
        JMenu viewMenu = new JMenu("View");
        
        JCheckBoxMenuItem showToolbar = new JCheckBoxMenuItem("Show Toolbar", true);
        showToolbar.addActionListener(e -> toolbarPanel.setVisible(showToolbar.isSelected()));
        
        JCheckBoxMenuItem showChat = new JCheckBoxMenuItem("Show Chat Panel", true);
        showChat.addActionListener(e -> {
            if (showChat.isSelected()) {
                rightTabbedPane.setSelectedIndex(0);
            }
        });
        
        viewMenu.add(showToolbar);
        viewMenu.add(showChat);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> showAboutDialog());
        
        helpMenu.add(about);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Handle window closing
     */
    private void handleWindowClosing() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit CollabBoard?",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            // Disconnect from server
            client.disconnect();
            
            // Exit application
            System.exit(0);
        }
    }
    
    /**
     * Show about dialog
     */
    private void showAboutDialog() {
        String message = "<html>" +
            "<h2>CollabBoard</h2>" +
            "<p>A collaborative whiteboard with group chat</p>" +
            "<p>Built with Java RMI</p>" +
            "<br>" +
            "<p><b>Features:</b></p>" +
            "<ul>" +
            "<li>Real-time collaborative drawing</li>" +
            "<li>Group chat with file sharing</li>" +
            "<li>Audio communication</li>" +
            "<li>Save and load whiteboard sessions</li>" +
            "</ul>" +
            "<br>" +
            "<p>Version 1.0.0</p>" +
            "</html>";
        
        JOptionPane.showMessageDialog(
            this,
            message,
            "About CollabBoard",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    // Getter methods for components
    public WhiteboardPanel getWhiteboardPanel() { return whiteboardPanel; }
    public ChatPanel getChatPanel() { return chatPanel; }
    public ToolbarPanel getToolbarPanel() { return toolbarPanel; }
    public UserListPanel getUserListPanel() { return userListPanel; }
    public FileSharePanel getFileSharePanel() { return fileSharePanel; }
    public AudioControlPanel getAudioControlPanel() { return audioControlPanel; }
    
    /**
     * Update window title with connection status
     */
    public void updateTitle(String status) {
        SwingUtilities.invokeLater(() -> {
            setTitle("CollabBoard - " + client.getUsername() + " (" + status + ")");
        });
    }
    
    /**
     * Show error message
     */
    public void showError(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        });
    }
    
    /**
     * Show information message
     */
    public void showInfo(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    /**
     * Switch to a specific tab
     */
    public void switchToTab(int tabIndex) {
        SwingUtilities.invokeLater(() -> {
            if (tabIndex >= 0 && tabIndex < rightTabbedPane.getTabCount()) {
                rightTabbedPane.setSelectedIndex(tabIndex);
            }
        });
    }
    
    /**
     * Switch to chat tab
     */
    public void switchToChatTab() {
        switchToTab(0);
    }
    
    /**
     * Switch to files tab
     */
    public void switchToFilesTab() {
        switchToTab(2);
    }
}
