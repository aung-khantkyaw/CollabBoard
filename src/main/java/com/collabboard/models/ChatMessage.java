package com.collabboard.models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a chat message in the group chat
 */
public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        TEXT, FILE, SYSTEM, AUDIO_NOTIFICATION
    }
    
    private String messageId;
    private String userId;
    private String username;
    private String content;
    private MessageType messageType;
    private LocalDateTime timestamp;
    private String fileUrl;
    private String fileName;
    private long fileSize;
    
    // Default constructor
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
        this.messageType = MessageType.TEXT;
    }
    
    // Constructor for text messages
    public ChatMessage(String userId, String username, String content) {
        this();
        this.userId = userId;
        this.username = username;
        this.content = content;
        this.messageId = generateMessageId();
    }
    
    // Constructor for file messages
    public ChatMessage(String userId, String username, String fileName, String fileUrl, long fileSize) {
        this();
        this.userId = userId;
        this.username = username;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.messageType = MessageType.FILE;
        this.content = "Shared file: " + fileName;
        this.messageId = generateMessageId();
    }
    
    // Constructor for system messages
    public static ChatMessage createSystemMessage(String content) {
        ChatMessage message = new ChatMessage();
        message.messageType = MessageType.SYSTEM;
        message.content = content;
        message.username = "System";
        message.messageId = message.generateMessageId();
        return message;
    }
    
    private String generateMessageId() {
        return userId + "_" + System.currentTimeMillis();
    }
    
    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }
    
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId='" + messageId + '\'' +
                ", username='" + username + '\'' +
                ", content='" + content + '\'' +
                ", messageType=" + messageType +
                ", timestamp=" + timestamp +
                '}';
    }
}
