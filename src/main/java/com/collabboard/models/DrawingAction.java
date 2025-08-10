package com.collabboard.models;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a drawing action on the whiteboard
 * This class is serializable to be transmitted via RMI
 */
public class DrawingAction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum ActionType {
        DRAW_LINE, DRAW_RECTANGLE, DRAW_CIRCLE, DRAW_TEXT, CLEAR_ALL, UNDO
    }
    
    private ActionType actionType;
    private List<Point> points;
    private Color color;
    private int strokeWidth;
    private String text;
    private String userId;
    private long timestamp;
    
    // Default constructor
    public DrawingAction() {
        this.points = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    // Constructor for drawing actions
    public DrawingAction(ActionType actionType, List<Point> points, Color color, int strokeWidth, String userId) {
        this();
        this.actionType = actionType;
        this.points = new ArrayList<>(points);
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.userId = userId;
    }
    
    // Constructor for text actions
    public DrawingAction(ActionType actionType, Point position, String text, Color color, String userId) {
        this();
        this.actionType = actionType;
        this.points = new ArrayList<>();
        this.points.add(position);
        this.text = text;
        this.color = color;
        this.userId = userId;
    }
    
    // Getters and Setters
    public ActionType getActionType() {
        return actionType;
    }
    
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
    
    public List<Point> getPoints() {
        return points;
    }
    
    public void setPoints(List<Point> points) {
        this.points = points;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public int getStrokeWidth() {
        return strokeWidth;
    }
    
    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "DrawingAction{" +
                "actionType=" + actionType +
                ", points=" + points.size() + " points" +
                ", color=" + color +
                ", strokeWidth=" + strokeWidth +
                ", userId='" + userId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
