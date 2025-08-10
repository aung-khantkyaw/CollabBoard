package com.collabboard.gui;

import com.collabboard.client.RMIClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel for audio communication controls
 * Note: This is a simplified implementation focusing on UI
 * Full audio implementation would require Java Sound API integration
 */
public class AudioControlPanel extends JPanel {
    
    private RMIClient client;
    
    // GUI Components
    private JButton joinAudioButton;
    private JButton leaveAudioButton;
    private JButton muteButton;
    private JSlider volumeSlider;
    private JLabel volumeLabel;
    private JLabel statusLabel;
    private JList<String> participantsList;
    private DefaultListModel<String> participantsModel;
    
    // Audio state
    private boolean inAudioSession = false;
    private boolean isMuted = false;
    private String currentSessionId = null;
    private Map<String, String> audioParticipants;
    
    public AudioControlPanel(RMIClient client) {
        this.client = client;
        this.audioParticipants = new HashMap<>();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        updateAudioControls();
    }
    
    /**
     * Initialize components
     */
    private void initializeComponents() {
        // Audio control buttons
        joinAudioButton = new JButton("🎤 Join Audio");
        joinAudioButton.setToolTipText("Join audio session");
        
        leaveAudioButton = new JButton("📵 Leave Audio");
        leaveAudioButton.setToolTipText("Leave audio session");
        leaveAudioButton.setEnabled(false);
        
        muteButton = new JButton("🔇 Mute");
        muteButton.setToolTipText("Toggle mute");
        muteButton.setEnabled(false);
        
        // Volume control
        volumeSlider = new JSlider(0, 100, 50);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setEnabled(false);
        
        volumeLabel = new JLabel("Volume: 50%");
        volumeLabel.setFont(volumeLabel.getFont().deriveFont(Font.PLAIN, 11f));
        
        // Status label
        statusLabel = new JLabel("Audio: Not connected");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 12f));
        statusLabel.setForeground(Color.RED);
        
        // Participants list
        participantsModel = new DefaultListModel<>();
        participantsList = new JList<>(participantsModel);
        participantsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        participantsList.setCellRenderer(new ParticipantCellRenderer());
    }
    
    /**
     * Setup panel layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Audio Communication"));
        
        // Control panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Status
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        controlPanel.add(statusLabel, gbc);
        
        // Join/Leave buttons
        gbc.gridwidth = 1; gbc.gridy = 1;
        gbc.gridx = 0;
        controlPanel.add(joinAudioButton, gbc);
        gbc.gridx = 1;
        controlPanel.add(leaveAudioButton, gbc);
        
        // Mute button
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        controlPanel.add(muteButton, gbc);
        
        // Volume control
        gbc.gridy = 3;
        controlPanel.add(volumeLabel, gbc);
        gbc.gridy = 4;
        controlPanel.add(volumeSlider, gbc);
        
        add(controlPanel, BorderLayout.NORTH);
        
        // Participants list
        JPanel participantsPanel = new JPanel(new BorderLayout());
        participantsPanel.setBorder(BorderFactory.createTitledBorder("Audio Participants"));
        
        JScrollPane scrollPane = new JScrollPane(participantsList);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        participantsPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(participantsPanel, BorderLayout.CENTER);
        
        // Instructions
        JPanel instructionsPanel = new JPanel();
        instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
        
        JLabel instruction1 = new JLabel("• Click 'Join Audio' to start voice chat");
        JLabel instruction2 = new JLabel("• Use mute button to toggle microphone");
        JLabel instruction3 = new JLabel("• Adjust volume with slider");
        
        instruction1.setFont(instruction1.getFont().deriveFont(Font.PLAIN, 10f));
        instruction2.setFont(instruction2.getFont().deriveFont(Font.PLAIN, 10f));
        instruction3.setFont(instruction3.getFont().deriveFont(Font.PLAIN, 10f));
        
        instructionsPanel.add(instruction1);
        instructionsPanel.add(instruction2);
        instructionsPanel.add(instruction3);
        
        add(instructionsPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        joinAudioButton.addActionListener(e -> joinAudioSession());
        leaveAudioButton.addActionListener(e -> leaveAudioSession());
        muteButton.addActionListener(e -> toggleMute());
        
        volumeSlider.addChangeListener(e -> {
            int volume = volumeSlider.getValue();
            volumeLabel.setText("Volume: " + volume + "%");
            // In full implementation, this would adjust actual audio volume
        });
    }
    
    /**
     * Join audio session
     */
    private void joinAudioSession() {
        if (!client.isConnected()) {
            showError("Not connected to server");
            return;
        }
        
        if (inAudioSession) {
            return;
        }
        
        try {
            // In a full implementation, this would:
            // 1. Initialize audio capture/playback
            // 2. Join RMI audio service
            // 3. Start audio streaming
            
            // For demo purposes, we'll simulate joining
            String sessionId = "audio_session_" + System.currentTimeMillis();
            onAudioSessionStarted(sessionId);
            
            // Simulate adding current user to participants
            onUserJoinedAudio(client.getUserId(), client.getUsername());
            
            JOptionPane.showMessageDialog(this,
                "Audio functionality is simulated in this demo.\n" +
                "In a full implementation, this would:\n" +
                "• Initialize microphone and speakers\n" +
                "• Connect to audio streaming service\n" +
                "• Enable real-time voice communication",
                "Audio Demo", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            showError("Failed to join audio session: " + e.getMessage());
        }
    }
    
    /**
     * Leave audio session
     */
    private void leaveAudioSession() {
        if (!inAudioSession) {
            return;
        }
        
        try {
            // In a full implementation, this would:
            // 1. Stop audio capture/playback
            // 2. Leave RMI audio service
            // 3. Clean up audio resources
            
            onUserLeftAudio(client.getUserId(), client.getUsername());
            onAudioSessionEnded(currentSessionId);
            
        } catch (Exception e) {
            showError("Failed to leave audio session: " + e.getMessage());
        }
    }
    
    /**
     * Toggle mute status
     */
    private void toggleMute() {
        if (!inAudioSession) {
            return;
        }
        
        isMuted = !isMuted;
        
        if (isMuted) {
            muteButton.setText("🔊 Unmute");
            muteButton.setToolTipText("Unmute microphone");
            statusLabel.setText("Audio: Connected (Muted)");
        } else {
            muteButton.setText("🔇 Mute");
            muteButton.setToolTipText("Mute microphone");
            statusLabel.setText("Audio: Connected");
        }
        
        // In full implementation, this would mute/unmute the microphone
        updateParticipantMuteStatus(client.getUserId(), isMuted);
    }
    
    /**
     * Update audio controls based on session state
     */
    private void updateAudioControls() {
        joinAudioButton.setEnabled(!inAudioSession && client.isConnected());
        leaveAudioButton.setEnabled(inAudioSession);
        muteButton.setEnabled(inAudioSession);
        volumeSlider.setEnabled(inAudioSession);
    }
    
    /**
     * Called when audio session starts
     */
    public void onAudioSessionStarted(String sessionId) {
        SwingUtilities.invokeLater(() -> {
            this.currentSessionId = sessionId;
            this.inAudioSession = true;
            this.isMuted = false;
            
            statusLabel.setText("Audio: Connected");
            statusLabel.setForeground(Color.GREEN);
            
            muteButton.setText("🔇 Mute");
            
            updateAudioControls();
        });
    }
    
    /**
     * Called when audio session ends
     */
    public void onAudioSessionEnded(String sessionId) {
        SwingUtilities.invokeLater(() -> {
            this.currentSessionId = null;
            this.inAudioSession = false;
            this.isMuted = false;
            
            statusLabel.setText("Audio: Not connected");
            statusLabel.setForeground(Color.RED);
            
            participantsModel.clear();
            audioParticipants.clear();
            
            updateAudioControls();
        });
    }
    
    /**
     * Called when user joins audio
     */
    public void onUserJoinedAudio(String userId, String username) {
        SwingUtilities.invokeLater(() -> {
            audioParticipants.put(userId, username);
            updateParticipantsList();
        });
    }
    
    /**
     * Called when user leaves audio
     */
    public void onUserLeftAudio(String userId, String username) {
        SwingUtilities.invokeLater(() -> {
            audioParticipants.remove(userId);
            updateParticipantsList();
        });
    }
    
    /**
     * Update participants list display
     */
    private void updateParticipantsList() {
        participantsModel.clear();
        
        for (Map.Entry<String, String> entry : audioParticipants.entrySet()) {
            String userId = entry.getKey();
            String username = entry.getValue();
            
            String displayName = username;
            if (userId.equals(client.getUserId())) {
                displayName += " (You)";
                if (isMuted) {
                    displayName += " 🔇";
                } else {
                    displayName += " 🎤";
                }
            } else {
                displayName += " 🎤"; // Assume others are not muted for demo
            }
            
            participantsModel.addElement(displayName);
        }
    }
    
    /**
     * Update mute status for a participant
     */
    private void updateParticipantMuteStatus(String userId, boolean muted) {
        // This would be used to update the visual indication of mute status
        updateParticipantsList();
    }
    
    /**
     * Custom cell renderer for participants list
     */
    private class ParticipantCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            String text = (String) value;
            if (text.contains("(You)")) {
                setFont(getFont().deriveFont(Font.BOLD));
                setForeground(isSelected ? Color.WHITE : Color.BLUE);
            }
            
            return this;
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Audio Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Enable/disable audio functionality based on connection
     */
    public void setConnectionEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            if (!enabled && inAudioSession) {
                // Force leave audio session if disconnected
                onAudioSessionEnded(currentSessionId);
            }
            
            updateAudioControls();
        });
    }
    
    /**
     * Get current audio session status
     */
    public boolean isInAudioSession() {
        return inAudioSession;
    }
    
    /**
     * Get current mute status
     */
    public boolean isMuted() {
        return isMuted;
    }
}
