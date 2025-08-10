package com.collabboard.gui;

import com.collabboard.client.RMIClient;
import com.collabboard.models.DrawingAction;
import com.collabboard.utils.DrawingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.rmi.RemoteException;

/**
 * Whiteboard panel for collaborative drawing
 */
public class WhiteboardPanel extends JPanel {
    
    private RMIClient client;
    
    // Drawing state
    private BufferedImage canvas;
    private Graphics2D canvasGraphics;
    private List<DrawingAction> localActions;
    
    // Current drawing state
    private DrawingAction.ActionType currentTool = DrawingAction.ActionType.DRAW_LINE;
    private Color currentColor = Color.BLACK;
    private int currentStrokeWidth = 2;
    
    // Mouse state
    private boolean isDrawing = false;
    private Point lastPoint;
    private List<Point> currentPoints;
    
    // Canvas properties
    private static final int CANVAS_WIDTH = 1000;
    private static final int CANVAS_HEIGHT = 700;
    
    public WhiteboardPanel(RMIClient client) {
        this.client = client;
        this.localActions = new ArrayList<>();
        this.currentPoints = new ArrayList<>();
        
        initializeCanvas();
        setupPanel();
        setupMouseListeners();
    }
    
    /**
     * Initialize the canvas
     */
    private void initializeCanvas() {
        canvas = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_RGB);
        canvasGraphics = canvas.createGraphics();
        canvasGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvasGraphics.setColor(Color.WHITE);
        canvasGraphics.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
    }
    
    /**
     * Setup panel properties
     */
    private void setupPanel() {
        setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLoweredBevelBorder());
        setFocusable(true);
    }
    
    /**
     * Setup mouse listeners for drawing
     */
    private void setupMouseListeners() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }
        };
        
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }
    
    /**
     * Handle mouse pressed event
     */
    private void handleMousePressed(MouseEvent e) {
        if (!client.isConnected()) {
            return;
        }
        
        isDrawing = true;
        lastPoint = e.getPoint();
        currentPoints.clear();
        currentPoints.add(new Point(lastPoint));
        
        // For text tool, handle immediately on click
        if (currentTool == DrawingAction.ActionType.DRAW_TEXT) {
            handleTextInput(e.getPoint());
            return;
        }
        
        requestFocusInWindow();
    }
    
    /**
     * Handle mouse dragged event
     */
    private void handleMouseDragged(MouseEvent e) {
        if (!isDrawing || !client.isConnected()) {
            return;
        }
        
        // Skip dragging for text tool since it's handled on click
        if (currentTool == DrawingAction.ActionType.DRAW_TEXT) {
            return;
        }
        
        Point currentPoint = e.getPoint();
        // For lines, keep all points; for rectangle/circle, keep only start and current
        switch (currentTool) {
            case DRAW_LINE:
                currentPoints.add(new Point(currentPoint));
                break;
            case DRAW_RECTANGLE:
            case DRAW_CIRCLE:
                if (currentPoints.size() == 1) {
                    currentPoints.add(new Point(currentPoint));
                } else {
                    currentPoints.set(1, new Point(currentPoint));
                }
                break;
            default:
                break;
        }
        repaint();
        lastPoint = currentPoint;
    }
    
    /**
     * Handle mouse released event
     */
    private void handleMouseReleased(MouseEvent e) {
        if (!isDrawing || !client.isConnected()) {
            return;
        }
        
        // Skip for text tool since it's already handled on click
        if (currentTool == DrawingAction.ActionType.DRAW_TEXT) {
            isDrawing = false;
            currentPoints.clear();
            return;
        }
        
        isDrawing = false;

        DrawingAction action = null;
        if (!currentPoints.isEmpty()) {
            switch (currentTool) {
                case DRAW_LINE:
                    if (currentPoints.size() > 1) {
                        action = new DrawingAction(
                            currentTool,
                            new ArrayList<>(currentPoints),
                            currentColor,
                            currentStrokeWidth,
                            client.getUserId()
                        );
                    }
                    break;
                case DRAW_RECTANGLE:
                case DRAW_CIRCLE:
                    if (currentPoints.size() >= 2) {
                        // Only use first and last point
                        ArrayList<Point> shapePoints = new ArrayList<>();
                        shapePoints.add(currentPoints.get(0));
                        shapePoints.add(currentPoints.get(currentPoints.size() - 1));
                        action = new DrawingAction(
                            currentTool,
                            shapePoints,
                            currentColor,
                            currentStrokeWidth,
                            client.getUserId()
                        );
                    }
                    break;
                default:
                    break;
            }
        }

        if (action != null) {
            localActions.add(action);
            DrawingUtils.renderDrawingAction(canvasGraphics, action);
            repaint();
            try {
                client.getWhiteboardService().addDrawingAction(action);
            } catch (RemoteException ex) {
                showError("Failed to send drawing action: " + ex.getMessage());
            }
        }

        currentPoints.clear();
    }
    
    /**
     * Handle text input for text tool
     */
    private void handleTextInput(Point position) {
        String text = JOptionPane.showInputDialog(this, "Enter text:", "Text Tool", JOptionPane.PLAIN_MESSAGE);
        if (text != null && !text.trim().isEmpty()) {
            DrawingAction action = new DrawingAction(
                DrawingAction.ActionType.DRAW_TEXT,
                position,
                text,
                currentColor,
                client.getUserId()
            );
            
            localActions.add(action);
            DrawingUtils.renderDrawingAction(canvasGraphics, action);
            repaint();
            
            try {
                client.getWhiteboardService().addDrawingAction(action);
            } catch (RemoteException ex) {
                showError("Failed to send drawing action: " + ex.getMessage());
            }
        }
        
        isDrawing = false;
        currentPoints.clear();
    }
    
    /**
     * Add a drawing action from remote client
     */
    public void addDrawingAction(DrawingAction action) {
        if (action == null) {
            return;
        }
        // Always add and render the action, even if it's from the local user
        localActions.add(action);
        DrawingUtils.renderDrawingAction(canvasGraphics, action);
        repaint();
    }
    
    /**
     * Clear the whiteboard
     */
    public void clearWhiteboard() {
        if (!client.isConnected()) {
            return;
        }
        
        try {
            client.getWhiteboardService().clearWhiteboard(client.getUserId());
        } catch (RemoteException e) {
            showError("Failed to clear whiteboard: " + e.getMessage());
        }
    }
    
    /**
     * Clear the local whiteboard (called from server callback)
     */
    public void clearWhiteboardLocal() {
        localActions.clear();
        
        // Clear canvas
        canvasGraphics.setColor(Color.WHITE);
        canvasGraphics.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        repaint();
    }
    
    /**
     * Undo last action
     */
    public void undo() {
        if (!client.isConnected()) {
            return;
        }
        
        try {
            client.getWhiteboardService().undoLastAction(client.getUserId());
        } catch (RemoteException e) {
            showError("Failed to undo: " + e.getMessage());
        }
    }
    
    /**
     * Undo last action locally (called from server callback)
     */
    public void undoLastAction() {
        if (!localActions.isEmpty()) {
            localActions.remove(localActions.size() - 1);
            redrawCanvas();
        }
    }
    
    /**
     * Save whiteboard to file
     */
    public void saveWhiteboard() {
        if (!client.isConnected()) {
            return;
        }
        
        String fileName = JOptionPane.showInputDialog(this, 
            "Enter filename to save:", "Save Whiteboard", JOptionPane.QUESTION_MESSAGE);
        
        if (fileName != null && !fileName.trim().isEmpty()) {
            try {
                boolean success = client.getWhiteboardService().saveWhiteboard(fileName, client.getUserId());
                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "Whiteboard saved successfully!", "Save Complete", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to save whiteboard.", "Save Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (RemoteException e) {
                showError("Failed to save whiteboard: " + e.getMessage());
            }
        }
    }
    
    /**
     * Load whiteboard from file
     */
    public void loadWhiteboard() {
        if (!client.isConnected()) {
            return;
        }
        
        String fileName = JOptionPane.showInputDialog(this, 
            "Enter filename to load:", "Load Whiteboard", JOptionPane.QUESTION_MESSAGE);
        
        if (fileName != null && !fileName.trim().isEmpty()) {
            try {
                boolean success = client.getWhiteboardService().loadWhiteboard(fileName, client.getUserId());
                if (!success) {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to load whiteboard. File may not exist.", "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (RemoteException e) {
                showError("Failed to load whiteboard: " + e.getMessage());
            }
        }
    }
    
    /**
     * Redraw the entire canvas
     */
    private void redrawCanvas() {
        // Clear canvas
        canvasGraphics.setColor(Color.WHITE);
        canvasGraphics.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        // Redraw all actions
        for (DrawingAction action : localActions) {
            DrawingUtils.renderDrawingAction(canvasGraphics, action);
        }
        
        repaint();
    }
    
    /**
     * Set current drawing tool
     */
    public void setCurrentTool(DrawingAction.ActionType tool) {
        this.currentTool = tool;
    }
    
    /**
     * Set current drawing color
     */
    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }
    
    /**
     * Set current stroke width
     */
    public void setCurrentStrokeWidth(int width) {
        this.currentStrokeWidth = width;
    }
    
    /**
     * Get current drawing tool
     */
    public DrawingAction.ActionType getCurrentTool() {
        return currentTool;
    }
    
    /**
     * Get current color
     */
    public Color getCurrentColor() {
        return currentColor;
    }
    
    /**
     * Get current stroke width
     */
    public int getCurrentStrokeWidth() {
        return currentStrokeWidth;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw the canvas
        g.drawImage(canvas, 0, 0, this);
        
        // Draw current drawing preview
        if (isDrawing && !currentPoints.isEmpty() && currentTool != DrawingAction.ActionType.DRAW_TEXT) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            DrawingAction previewAction = null;
            switch (currentTool) {
                case DRAW_LINE:
                    if (currentPoints.size() > 1) {
                        previewAction = new DrawingAction(
                            currentTool,
                            new ArrayList<>(currentPoints),
                            currentColor,
                            currentStrokeWidth,
                            client.getUserId()
                        );
                    }
                    break;
                case DRAW_RECTANGLE:
                case DRAW_CIRCLE:
                    if (currentPoints.size() >= 2) {
                        ArrayList<Point> shapePoints = new ArrayList<>();
                        shapePoints.add(currentPoints.get(0));
                        shapePoints.add(currentPoints.get(currentPoints.size() - 1));
                        previewAction = new DrawingAction(
                            currentTool,
                            shapePoints,
                            currentColor,
                            currentStrokeWidth,
                            client.getUserId()
                        );
                    }
                    break;
                default:
                    break;
            }
            if (previewAction != null) {
                DrawingUtils.renderDrawingAction(g2d, previewAction);
            }
            g2d.dispose();
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }
    
    /**
     * Initialize whiteboard with existing actions (when connecting)
     */
    public void initializeWithActions(List<DrawingAction> actions) {
        localActions.clear();
        
        // Clear canvas
        canvasGraphics.setColor(Color.WHITE);
        canvasGraphics.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        
        // Add all actions
        for (DrawingAction action : actions) {
            localActions.add(action);
            DrawingUtils.renderDrawingAction(canvasGraphics, action);
        }
        
        repaint();
    }
    
    /**
     * Get canvas as image for export
     */
    public BufferedImage getCanvasImage() {
        return canvas;
    }
}
