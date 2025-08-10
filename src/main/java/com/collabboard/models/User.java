package com.collabboard.models;

import java.io.Serializable;

/**
 * Represents a user in the collaborative whiteboard system
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String userId;
    private String username;
    private String ipAddress;
    private boolean isOnline;
    private long lastActivity;
    private boolean isAudioEnabled;
    private boolean isMuted;
    
    // Default constructor
    public User() {
        this.lastActivity = System.currentTimeMillis();
        this.isOnline = true;
        this.isAudioEnabled = false;
        this.isMuted = false;
    }
    
    // Constructor
    public User(String userId, String username, String ipAddress) {
        this();
        this.userId = userId;
        this.username = username;
        this.ipAddress = ipAddress;
    }
    
    // Getters and Setters
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
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setOnline(boolean online) {
        isOnline = online;
    }
    
    public long getLastActivity() {
        return lastActivity;
    }
    
    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }
    
    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
    }
    
    public boolean isAudioEnabled() {
        return isAudioEnabled;
    }
    
    public void setAudioEnabled(boolean audioEnabled) {
        isAudioEnabled = audioEnabled;
    }
    
    public boolean isMuted() {
        return isMuted;
    }
    
    public void setMuted(boolean muted) {
        isMuted = muted;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return userId != null ? userId.equals(user.userId) : user.userId == null;
    }
    
    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", isOnline=" + isOnline +
                ", isAudioEnabled=" + isAudioEnabled +
                ", isMuted=" + isMuted +
                '}';
    }
}
