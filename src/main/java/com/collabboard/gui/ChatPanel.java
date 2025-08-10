package com.collabboard.gui;

import com.collabboard.client.RMIClient;
import com.collabboard.models.ChatMessage;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.rmi.RemoteException;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Chat panel for group messaging
 */
public class ChatPanel extends JPanel {
    
    private RMIClient client;
    
    // GUI Components
    private JTextPane chatDisplay;
    private JTextField messageInput;
    private JButton sendButton;
    private JLabel typingLabel;
    
    // Message formatting
    private StyledDocument chatDocument;
    private SimpleAttributeSet systemStyle;
    private SimpleAttributeSet userStyle;
    private SimpleAttributeSet ownMessageStyle;
    private SimpleAttributeSet timestampStyle;
    
    // Typing indicator
    private Map<String, String> typingUsers;
    private Timer typingTimer;
    
    public ChatPanel(RMIClient client) {
        this.client = client;
        this.typingUsers = new HashMap<>();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupStyles();
        setupTypingIndicator();
    }
    
    /**
     * Initialize chat components
     */
    private void initializeComponents() {
        // Chat display
        chatDisplay = new JTextPane();
        chatDisplay.setEditable(false);
        chatDisplay.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        chatDocument = chatDisplay.getStyledDocument();
        
        // Message input
        messageInput = new JTextField();
        messageInput.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        // Send button
        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(60, 25));
        
        // Typing indicator
        typingLabel = new JLabel(" ");
        typingLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        typingLabel.setForeground(Color.GRAY);
    }
    
    /**
     * Setup panel layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Group Chat"));
        
        // Chat display with scroll
        JScrollPane scrollPane = new JScrollPane(chatDisplay);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Input area
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageInput, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        
        inputPanel.add(messagePanel, BorderLayout.CENTER);
        inputPanel.add(typingLabel, BorderLayout.SOUTH);
        
        add(inputPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Send button action
        sendButton.addActionListener(e -> sendMessage());
        
        // Enter key to send message
        messageInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                } else {
                    // Notify typing
                    notifyTyping(true);
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (messageInput.getText().trim().isEmpty()) {
                    notifyTyping(false);
                }
            }
        });
        
        // Focus listener for message input
        messageInput.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                notifyTyping(false);
            }
        });
    }
    
    /**
     * Setup text styles for different message types
     */
    private void setupStyles() {
        // System message style
        systemStyle = new SimpleAttributeSet();
        StyleConstants.setItalic(systemStyle, true);
        StyleConstants.setForeground(systemStyle, Color.BLUE);
        StyleConstants.setFontSize(systemStyle, 11);
        
        // Regular user message style
        userStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(userStyle, Color.BLACK);
        StyleConstants.setFontSize(userStyle, 12);
        
        // Own message style
        ownMessageStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(ownMessageStyle, new Color(0, 100, 0));
        StyleConstants.setFontSize(ownMessageStyle, 12);
        StyleConstants.setBold(ownMessageStyle, true);
        
        // Timestamp style
        timestampStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(timestampStyle, Color.GRAY);
        StyleConstants.setFontSize(timestampStyle, 10);
    }
    
    /**
     * Setup typing indicator timer
     */
    private void setupTypingIndicator() {
        typingTimer = new Timer(3000, e -> {
            // Clear typing notification after 3 seconds
            notifyTyping(false);
        });
        typingTimer.setRepeats(false);
    }
    
    /**
     * Send chat message
     */
    private void sendMessage() {
        String messageText = messageInput.getText().trim();
        
        if (messageText.isEmpty() || !client.isConnected()) {
            return;
        }
        
        try {
            // Create chat message
            ChatMessage message = new ChatMessage(
                client.getUserId(),
                client.getUsername(),
                messageText
            );
            
            // Send to server
            client.getChatService().sendMessage(message);
            
            // Clear input
            messageInput.setText("");
            
            // Stop typing notification
            notifyTyping(false);
            
        } catch (RemoteException e) {
            showError("Failed to send message: " + e.getMessage());
        }
    }
    
    /**
     * Add a chat message to the display
     */
    public void addMessage(ChatMessage message) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Choose style based on message type and sender
                SimpleAttributeSet messageStyle;
                if (message.getMessageType() == ChatMessage.MessageType.SYSTEM) {
                    messageStyle = systemStyle;
                } else if (client.getUserId().equals(message.getUserId())) {
                    messageStyle = ownMessageStyle;
                } else {
                    messageStyle = userStyle;
                }
                
                // Format timestamp
                String timestamp = message.getTimestamp().format(
                    DateTimeFormatter.ofPattern("HH:mm:ss"));
                
                // Add timestamp
                chatDocument.insertString(chatDocument.getLength(), 
                    "[" + timestamp + "] ", timestampStyle);
                
                // Add username (if not system message)
                if (message.getMessageType() != ChatMessage.MessageType.SYSTEM) {
                    String username = client.getUserId().equals(message.getUserId()) ? 
                        "You" : message.getUsername();
                    chatDocument.insertString(chatDocument.getLength(), 
                        username + ": ", messageStyle);
                }
                
                // Add message content
                chatDocument.insertString(chatDocument.getLength(), 
                    message.getContent() + "\n", messageStyle);
                
                // Auto-scroll to bottom
                chatDisplay.setCaretPosition(chatDocument.getLength());
                
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Add a system message
     */
    public void addSystemMessage(String message) {
        ChatMessage systemMessage = ChatMessage.createSystemMessage(message);
        addMessage(systemMessage);
    }
    
    /**
     * Update typing status display
     */
    public void updateTypingStatus(String userId, String username, boolean isTyping) {
        SwingUtilities.invokeLater(() -> {
            if (isTyping) {
                typingUsers.put(userId, username);
            } else {
                typingUsers.remove(userId);
            }
            
            updateTypingLabel();
        });
    }
    
    /**
     * Update the typing indicator label
     */
    private void updateTypingLabel() {
        if (typingUsers.isEmpty()) {
            typingLabel.setText(" ");
        } else if (typingUsers.size() == 1) {
            String username = typingUsers.values().iterator().next();
            typingLabel.setText(username + " is typing...");
        } else {
            typingLabel.setText(typingUsers.size() + " users are typing...");
        }
    }
    
    /**
     * Notify server about typing status
     */
    private void notifyTyping(boolean isTyping) {
        if (!client.isConnected()) {
            return;
        }
        
        try {
            client.getChatService().notifyTyping(
                client.getUserId(), 
                client.getUsername(), 
                isTyping
            );
            
            if (isTyping) {
                typingTimer.restart();
            } else {
                typingTimer.stop();
            }
            
        } catch (RemoteException e) {
            // Ignore typing notification errors
        }
    }
    
    /**
     * Clear chat display
     */
    public void clearChat() {
        SwingUtilities.invokeLater(() -> {
            chatDisplay.setText("");
        });
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Chat Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Enable or disable chat input
     */
    public void setInputEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            messageInput.setEnabled(enabled);
            sendButton.setEnabled(enabled);
            
            if (!enabled) {
                messageInput.setText("");
                typingLabel.setText("Disconnected");
            } else {
                typingLabel.setText(" ");
            }
        });
    }
    
    /**
     * Focus on message input
     */
    public void focusInput() {
        SwingUtilities.invokeLater(() -> {
            messageInput.requestFocusInWindow();
        });
    }
    
    /**
     * Get message input field for external access
     */
    public JTextField getMessageInput() {
        return messageInput;
    }
}
