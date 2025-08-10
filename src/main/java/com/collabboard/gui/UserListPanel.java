package com.collabboard.gui;

import com.collabboard.client.RMIClient;
import com.collabboard.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Panel to display online users and their status
 */
public class UserListPanel extends JPanel {
    
    private RMIClient client;
    
    // GUI Components
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JLabel userCountLabel;
    
    // Table columns
    private static final String[] COLUMN_NAMES = {"Username", "Status", "Audio"};
    private static final int USERNAME_COLUMN = 0;
    private static final int STATUS_COLUMN = 1;
    private static final int AUDIO_COLUMN = 2;
    
    public UserListPanel(RMIClient client) {
        this.client = client;
        
        initializeComponents();
        setupLayout();
        setupTable();
    }
    
    /**
     * Initialize components
     */
    private void initializeComponents() {
        // Table model
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == STATUS_COLUMN || columnIndex == AUDIO_COLUMN) {
                    return Icon.class;
                }
                return String.class;
            }
        };
        
        // User table
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(25);
        userTable.getTableHeader().setReorderingAllowed(false);
        
        // User count label
        userCountLabel = new JLabel("Users: 0");
        userCountLabel.setFont(userCountLabel.getFont().deriveFont(Font.BOLD));
    }
    
    /**
     * Setup panel layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Online Users"));
        
        // Table with scroll pane
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setPreferredSize(new Dimension(250, 300));
        
        add(scrollPane, BorderLayout.CENTER);
        
        // User count at bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(userCountLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Setup table properties and renderers
     */
    private void setupTable() {
        // Set column widths
        userTable.getColumnModel().getColumn(USERNAME_COLUMN).setPreferredWidth(120);
        userTable.getColumnModel().getColumn(STATUS_COLUMN).setPreferredWidth(60);
        userTable.getColumnModel().getColumn(AUDIO_COLUMN).setPreferredWidth(60);
        
        // Custom renderer for status and audio columns
        userTable.getColumnModel().getColumn(STATUS_COLUMN).setCellRenderer(new StatusRenderer());
        userTable.getColumnModel().getColumn(AUDIO_COLUMN).setCellRenderer(new AudioRenderer());
        
        // Add context menu
        setupContextMenu();
    }
    
    /**
     * Setup context menu for user table
     */
    private void setupContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();
        
        JMenuItem refreshItem = new JMenuItem("Refresh User List");
        refreshItem.addActionListener(e -> refreshUserList());
        
        JMenuItem viewProfileItem = new JMenuItem("View User Info");
        viewProfileItem.addActionListener(e -> viewSelectedUserInfo());
        
        contextMenu.add(refreshItem);
        contextMenu.addSeparator();
        contextMenu.add(viewProfileItem);
        
        userTable.setComponentPopupMenu(contextMenu);
    }
    
    /**
     * Update the user list
     */
    public void updateUserList(List<User> users) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("UserListPanel: Updating user list with " + users.size() + " users");
            
            // Clear existing data
            tableModel.setRowCount(0);
            
            // Add users to table
            for (User user : users) {
                System.out.println("UserListPanel: Adding user " + user.getUsername() + " (online: " + user.isOnline() + ")");
                Object[] rowData = {
                    user.getUsername(),
                    user.isOnline() ? "online" : "offline",
                    user.isAudioEnabled() ? "enabled" : "disabled"
                };
                tableModel.addRow(rowData);
            }
            
            // Update user count
            userCountLabel.setText("Users: " + users.size());
            
            // Highlight current user
            highlightCurrentUser(users);
        });
    }
    
    /**
     * Add a single user to the list
     */
    public void addUser(User user) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("UserListPanel: Adding single user " + user.getUsername());
            Object[] rowData = {
                user.getUsername(),
                user.isOnline() ? "online" : "offline",
                user.isAudioEnabled() ? "enabled" : "disabled"
            };
            tableModel.addRow(rowData);
            
            updateUserCount();
        });
    }
    
    /**
     * Remove a user from the list
     */
    public void removeUser(User user) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String username = (String) tableModel.getValueAt(i, USERNAME_COLUMN);
                if (username.equals(user.getUsername())) {
                    tableModel.removeRow(i);
                    break;
                }
            }
            
            updateUserCount();
        });
    }
    
    /**
     * Update user count label
     */
    private void updateUserCount() {
        userCountLabel.setText("Users: " + tableModel.getRowCount());
    }
    
    /**
     * Highlight the current user in the table
     */
    private void highlightCurrentUser(List<User> users) {
        String currentUsername = client.getUsername();
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String username = (String) tableModel.getValueAt(i, USERNAME_COLUMN);
            if (username.equals(currentUsername)) {
                // You could add special formatting here for current user
                break;
            }
        }
    }
    
    /**
     * Refresh user list from server
     */
    private void refreshUserList() {
        if (!client.isConnected()) {
            return;
        }
        
        try {
            List<User> users = client.getChatService().getOnlineUsers();
            updateUserList(users);
        } catch (Exception e) {
            showError("Failed to refresh user list: " + e.getMessage());
        }
    }
    
    /**
     * View information about selected user
     */
    private void viewSelectedUserInfo() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }
        
        String username = (String) tableModel.getValueAt(selectedRow, USERNAME_COLUMN);
        String status = (String) tableModel.getValueAt(selectedRow, STATUS_COLUMN);
        String audio = (String) tableModel.getValueAt(selectedRow, AUDIO_COLUMN);
        
        String info = String.format(
            "User Information:\n\n" +
            "Username: %s\n" +
            "Status: %s\n" +
            "Audio: %s",
            username, status, audio
        );
        
        JOptionPane.showMessageDialog(this, info, "User Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Clear user list
     */
    public void clearUserList() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            userCountLabel.setText("Users: 0");
        });
    }
    
    /**
     * Custom renderer for status column
     */
    private class StatusRenderer extends JLabel implements TableCellRenderer {
        public StatusRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            String status = (String) value;
            
            if ("online".equals(status)) {
                setIcon(createColorIcon(Color.GREEN));
                setText("Online");
            } else {
                setIcon(createColorIcon(Color.RED));
                setText("Offline");
            }
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            
            return this;
        }
    }
    
    /**
     * Custom renderer for audio column
     */
    private class AudioRenderer extends JLabel implements TableCellRenderer {
        public AudioRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            String audio = (String) value;
            
            if ("enabled".equals(audio)) {
                setIcon(createColorIcon(Color.BLUE));
                setText("🔊");
            } else {
                setIcon(createColorIcon(Color.GRAY));
                setText("🔇");
            }
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            
            return this;
        }
    }
    
    /**
     * Create a small colored icon
     */
    private Icon createColorIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillOval(x, y + 2, 8, 8);
                g.setColor(Color.BLACK);
                g.drawOval(x, y + 2, 8, 8);
            }
            
            @Override
            public int getIconWidth() {
                return 10;
            }
            
            @Override
            public int getIconHeight() {
                return 12;
            }
        };
    }
}
