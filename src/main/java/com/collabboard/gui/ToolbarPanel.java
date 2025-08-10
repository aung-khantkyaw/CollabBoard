package com.collabboard.gui;

import com.collabboard.client.RMIClient;
import com.collabboard.models.DrawingAction;
import com.collabboard.utils.DrawingUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Toolbar panel with drawing tools and controls
 */
public class ToolbarPanel extends JPanel {
    
    private RMIClient client;
    private WhiteboardPanel whiteboardPanel;
    
    // Tool buttons
    private JToggleButton penTool;
    private JToggleButton rectangleTool;
    private JToggleButton circleTool;
    private JToggleButton textTool;
    
    // Action buttons
    private JButton clearButton;
    private JButton undoButton;
    private JButton saveButton;
    private JButton loadButton;
    
    // Color and stroke controls
    private JButton colorButton;
    private JComboBox<Integer> strokeWidthCombo;
    private JPanel colorPreview;
    
    // Button groups
    private ButtonGroup toolGroup;
    
    public ToolbarPanel(RMIClient client) {
        this.client = client;
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        setupToolGroup();
    }
    
    /**
     * Set the whiteboard panel reference
     */
    public void setWhiteboardPanel(WhiteboardPanel whiteboardPanel) {
        this.whiteboardPanel = whiteboardPanel;
    }
    
    /**
     * Initialize toolbar components
     */
    private void initializeComponents() {
        // Tool buttons
        penTool = new JToggleButton("✏️ Pen");
        penTool.setToolTipText("Free drawing tool");
        penTool.setSelected(true);
        
        rectangleTool = new JToggleButton("⬜ Rectangle");
        rectangleTool.setToolTipText("Draw rectangles");
        
        circleTool = new JToggleButton("⭕ Circle");
        circleTool.setToolTipText("Draw circles");
        
        textTool = new JToggleButton("📝 Text");
        textTool.setToolTipText("Add text");
        
        // Action buttons
        clearButton = new JButton("🗑️ Clear");
        clearButton.setToolTipText("Clear whiteboard");
        
        undoButton = new JButton("↶ Undo");
        undoButton.setToolTipText("Undo last action");
        
        saveButton = new JButton("💾 Save");
        saveButton.setToolTipText("Save whiteboard");
        
        loadButton = new JButton("📁 Load");
        loadButton.setToolTipText("Load whiteboard");
        
        // Color controls
        colorButton = new JButton("🎨 Color");
        colorButton.setToolTipText("Choose drawing color");
        
        colorPreview = new JPanel();
        colorPreview.setBackground(Color.BLACK);
        colorPreview.setPreferredSize(new Dimension(30, 30));
        colorPreview.setBorder(BorderFactory.createLoweredBevelBorder());
        
        // Stroke width combo
        Integer[] strokeWidths = {1, 2, 3, 5, 8, 12, 16, 20};
        strokeWidthCombo = new JComboBox<>(strokeWidths);
        strokeWidthCombo.setSelectedItem(2);
        strokeWidthCombo.setToolTipText("Stroke width");
    }
    
    /**
     * Setup toolbar layout
     */
    private void setupLayout() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        setBorder(BorderFactory.createRaisedBevelBorder());
        
        // Add tool buttons
        add(new JLabel("Tools:"));
        add(penTool);
        add(rectangleTool);
        add(circleTool);
        add(textTool);
        
        add(new JSeparator(SwingConstants.VERTICAL));
        
        // Add action buttons
        add(new JLabel("Actions:"));
        add(undoButton);
        add(clearButton);
        
        add(new JSeparator(SwingConstants.VERTICAL));
        
        // Add file operations
        add(new JLabel("File:"));
        add(saveButton);
        add(loadButton);
        
        add(new JSeparator(SwingConstants.VERTICAL));
        
        // Add drawing properties
        add(new JLabel("Style:"));
        add(colorButton);
        add(colorPreview);
        add(new JLabel("Width:"));
        add(strokeWidthCombo);
        
        add(new JSeparator(SwingConstants.VERTICAL));
        
        // Add connection status
        JLabel statusLabel = new JLabel("Connected");
        statusLabel.setForeground(Color.GREEN);
        add(statusLabel);
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        // Tool selection handlers
        penTool.addActionListener(e -> selectTool(DrawingAction.ActionType.DRAW_LINE));
        rectangleTool.addActionListener(e -> selectTool(DrawingAction.ActionType.DRAW_RECTANGLE));
        circleTool.addActionListener(e -> selectTool(DrawingAction.ActionType.DRAW_CIRCLE));
        textTool.addActionListener(e -> selectTool(DrawingAction.ActionType.DRAW_TEXT));
        
        // Action button handlers
        clearButton.addActionListener(e -> handleClear());
        undoButton.addActionListener(e -> handleUndo());
        saveButton.addActionListener(e -> handleSave());
        loadButton.addActionListener(e -> handleLoad());
        
        // Color selection handler
        colorButton.addActionListener(e -> handleColorSelection());
        
        // Stroke width handler
        strokeWidthCombo.addActionListener(e -> handleStrokeWidthChange());
    }
    
    /**
     * Setup tool button group
     */
    private void setupToolGroup() {
        toolGroup = new ButtonGroup();
        toolGroup.add(penTool);
        toolGroup.add(rectangleTool);
        toolGroup.add(circleTool);
        toolGroup.add(textTool);
    }
    
    /**
     * Select drawing tool
     */
    private void selectTool(DrawingAction.ActionType tool) {
        if (whiteboardPanel != null) {
            whiteboardPanel.setCurrentTool(tool);
        }
        
        // Update UI feedback
        switch (tool) {
            case DRAW_LINE:
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                break;
            case DRAW_RECTANGLE:
            case DRAW_CIRCLE:
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                break;
            case DRAW_TEXT:
                setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                break;
        }
    }
    
    /**
     * Handle clear button
     */
    private void handleClear() {
        if (!client.isConnected()) {
            showError("Not connected to server");
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to clear the entire whiteboard?",
            "Clear Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION && whiteboardPanel != null) {
            whiteboardPanel.clearWhiteboard();
        }
    }
    
    /**
     * Handle undo button
     */
    private void handleUndo() {
        if (!client.isConnected()) {
            showError("Not connected to server");
            return;
        }
        
        if (whiteboardPanel != null) {
            whiteboardPanel.undo();
        }
    }
    
    /**
     * Handle save button
     */
    private void handleSave() {
        if (!client.isConnected()) {
            showError("Not connected to server");
            return;
        }
        
        if (whiteboardPanel != null) {
            whiteboardPanel.saveWhiteboard();
        }
    }
    
    /**
     * Handle load button
     */
    private void handleLoad() {
        if (!client.isConnected()) {
            showError("Not connected to server");
            return;
        }
        
        if (whiteboardPanel != null) {
            whiteboardPanel.loadWhiteboard();
        }
    }
    
    /**
     * Handle color selection
     */
    private void handleColorSelection() {
        Color currentColor = whiteboardPanel != null ? whiteboardPanel.getCurrentColor() : Color.BLACK;
        
        Color newColor = JColorChooser.showDialog(
            this,
            "Choose Drawing Color",
            currentColor
        );
        
        if (newColor != null) {
            colorPreview.setBackground(newColor);
            colorPreview.repaint();
            
            if (whiteboardPanel != null) {
                whiteboardPanel.setCurrentColor(newColor);
            }
        }
    }
    
    /**
     * Handle stroke width change
     */
    private void handleStrokeWidthChange() {
        Integer selectedWidth = (Integer) strokeWidthCombo.getSelectedItem();
        if (selectedWidth != null && whiteboardPanel != null) {
            whiteboardPanel.setCurrentStrokeWidth(selectedWidth);
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Update connection status
     */
    public void updateConnectionStatus(boolean connected) {
        // Find and update status label
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                if (label.getText().contains("Connected") || label.getText().contains("Disconnected")) {
                    if (connected) {
                        label.setText("Connected");
                        label.setForeground(Color.GREEN);
                    } else {
                        label.setText("Disconnected");
                        label.setForeground(Color.RED);
                    }
                    break;
                }
            }
        }
        
        // Enable/disable buttons based on connection
        enableButtons(connected);
    }
    
    /**
     * Enable or disable buttons based on connection status
     */
    private void enableButtons(boolean enabled) {
        clearButton.setEnabled(enabled);
        undoButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
        loadButton.setEnabled(enabled);
        
        penTool.setEnabled(enabled);
        rectangleTool.setEnabled(enabled);
        circleTool.setEnabled(enabled);
        textTool.setEnabled(enabled);
    }
    
    /**
     * Get quick access to color palette
     */
    private void showColorPalette() {
        JPopupMenu colorPalette = new JPopupMenu();
        
        Color[] predefinedColors = DrawingUtils.getPredefinedColors();
        
        JPanel colorGrid = new JPanel(new GridLayout(3, 4, 2, 2));
        
        for (Color color : predefinedColors) {
            JButton colorBtn = new JButton();
            colorBtn.setBackground(color);
            colorBtn.setPreferredSize(new Dimension(30, 30));
            colorBtn.setBorder(BorderFactory.createRaisedBevelBorder());
            colorBtn.addActionListener(e -> {
                colorPreview.setBackground(color);
                colorPreview.repaint();
                if (whiteboardPanel != null) {
                    whiteboardPanel.setCurrentColor(color);
                }
                colorPalette.setVisible(false);
            });
            colorGrid.add(colorBtn);
        }
        
        colorPalette.add(colorGrid);
        colorPalette.show(colorButton, 0, colorButton.getHeight());
    }
    
    /**
     * Initialize with whiteboard panel reference
     */
    public void initialize(WhiteboardPanel whiteboardPanel) {
        this.whiteboardPanel = whiteboardPanel;
        
        // Set initial values
        if (whiteboardPanel != null) {
            whiteboardPanel.setCurrentTool(DrawingAction.ActionType.DRAW_LINE);
            whiteboardPanel.setCurrentColor(Color.BLACK);
            whiteboardPanel.setCurrentStrokeWidth(2);
        }
    }
}
