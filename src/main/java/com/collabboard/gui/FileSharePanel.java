package com.collabboard.gui;

import com.collabboard.client.RMIClient;
import com.collabboard.models.FileTransfer;
import com.collabboard.utils.FileUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Panel for file sharing functionality
 */
public class FileSharePanel extends JPanel {
    
    private RMIClient client;
    
    // GUI Components
    private JTable fileTable;
    private DefaultTableModel tableModel;
    private JButton uploadButton;
    private JButton downloadButton;
    private JButton deleteButton;
    private JLabel statusLabel;
    
    // File tracking
    private java.util.List<FileTransfer> fileList; // Store actual file objects with IDs
    
    // Table columns
    private static final String[] COLUMN_NAMES = {"File Name", "Size", "Uploader", "Type"};
    private static final int FILENAME_COLUMN = 0;
    private static final int SIZE_COLUMN = 1;
    private static final int UPLOADER_COLUMN = 2;
    private static final int TYPE_COLUMN = 3;
    
    public FileSharePanel(RMIClient client) {
        this.client = client;
        this.fileList = new java.util.ArrayList<>();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadSharedFiles();
    }
    
    /**
     * Initialize components
     */
    private void initializeComponents() {
        // Table model
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // File table
        fileTable = new JTable(tableModel);
        fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fileTable.setRowHeight(25);
        
        // Buttons
        uploadButton = new JButton("📁 Upload File");
        downloadButton = new JButton("⬇️ Download");
        deleteButton = new JButton("🗑️ Delete");
        
        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 10f));
    }
    
    /**
     * Setup panel layout
     */
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("File Sharing"));
        
        // Table with scroll pane
        JScrollPane scrollPane = new JScrollPane(fileTable);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(uploadButton);
        buttonPanel.add(downloadButton);
        buttonPanel.add(deleteButton);
        
        add(buttonPanel, BorderLayout.NORTH);
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        uploadButton.addActionListener(e -> uploadFile());
        downloadButton.addActionListener(e -> downloadSelectedFile());
        deleteButton.addActionListener(e -> deleteSelectedFile());
        
        // Enable/disable buttons based on selection
        fileTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = fileTable.getSelectedRow() != -1;
            downloadButton.setEnabled(hasSelection);
            
            // Only enable delete for own files
            if (hasSelection) {
                String uploader = (String) tableModel.getValueAt(fileTable.getSelectedRow(), UPLOADER_COLUMN);
                deleteButton.setEnabled(client.getUsername().equals(uploader));
            } else {
                deleteButton.setEnabled(false);
            }
        });
        
        // Initially disable action buttons
        downloadButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    /**
     * Upload a file
     */
    private void uploadFile() {
        if (!client.isConnected()) {
            showError("Not connected to server");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select File to Upload");
        
        int result = fileChooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File selectedFile = fileChooser.getSelectedFile();
        
        try {
            // Check file size
            if (!FileUtils.isFileSizeValid(selectedFile.length())) {
                showError("File is too large. Maximum size: " + 
                         FileUtils.formatFileSize(FileUtils.MAX_FILE_SIZE));
                return;
            }
            
            // Check file type
            String extension = FileUtils.getFileExtension(selectedFile.getName());
            if (!FileUtils.isFileTypeAllowed(extension)) {
                showError("File type not allowed: " + extension);
                return;
            }
            
            statusLabel.setText("Uploading file...");
            uploadButton.setEnabled(false);
            
            // Read file data
            byte[] fileData = FileUtils.readFileAsBytes(selectedFile.getAbsolutePath());
            
            // Create file transfer object
            FileTransfer fileTransfer = new FileTransfer(
                selectedFile.getName(),
                extension,
                fileData,
                client.getUserId(),
                client.getUsername()
            );
            
            // Upload to server
            String fileId = client.getFileService().uploadFile(fileTransfer);
            
            if (fileId != null && !fileId.trim().isEmpty()) {
                // Set the file ID that was returned by the server
                fileTransfer.setFileId(fileId);
                
                // Share the file
                client.getFileService().shareFile(fileId, client.getUserId());
                
                statusLabel.setText("File uploaded successfully");
                
                // Refresh file list
                loadSharedFiles();
            } else {
                showError("Failed to upload file - no file ID returned");
                statusLabel.setText("Upload failed");
            }
            
        } catch (Exception e) {
            showError("Upload error: " + e.getMessage());
            statusLabel.setText("Upload failed");
        } finally {
            uploadButton.setEnabled(true);
        }
    }
    
    /**
     * Download selected file
     */
    private void downloadSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow == -1 || !client.isConnected()) {
            return;
        }
        
        String fileName = (String) tableModel.getValueAt(selectedRow, FILENAME_COLUMN);
        
        // Find file ID (we'll store it as user data)
        String fileId = getFileIdForRow(selectedRow);
        if (fileId == null || fileId.trim().isEmpty()) {
            showError("File ID not found - unable to download");
            return;
        }
        
        // Choose download location
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save File As");
        fileChooser.setSelectedFile(new File(fileName));
        
        int result = fileChooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        
        File saveFile = fileChooser.getSelectedFile();
        
        try {
            statusLabel.setText("Downloading file...");
            downloadButton.setEnabled(false);
            
            // Download from server
            System.out.println("Attempting to download file with ID: " + fileId);
            FileTransfer fileTransfer = client.getFileService().downloadFile(fileId);
            
            if (fileTransfer != null) {
                // Save to local file
                FileUtils.writeBytesToFile(saveFile.getAbsolutePath(), fileTransfer.getData());
                
                statusLabel.setText("File downloaded successfully");
                JOptionPane.showMessageDialog(this, 
                    "File saved to: " + saveFile.getAbsolutePath(),
                    "Download Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.err.println("Server returned null for file ID: " + fileId);
                showError("File not found on server");
                statusLabel.setText("Download failed");
            }
            
        } catch (Exception e) {
            showError("Download error: " + e.getMessage());
            statusLabel.setText("Download failed");
        } finally {
            downloadButton.setEnabled(true);
        }
    }
    
    /**
     * Delete selected file
     */
    private void deleteSelectedFile() {
        int selectedRow = fileTable.getSelectedRow();
        if (selectedRow == -1 || !client.isConnected()) {
            return;
        }
        
        String fileName = (String) tableModel.getValueAt(selectedRow, FILENAME_COLUMN);
        String uploader = (String) tableModel.getValueAt(selectedRow, UPLOADER_COLUMN);
        
        // Check if user can delete this file
        if (!client.getUsername().equals(uploader)) {
            showError("You can only delete your own files");
            return;
        }
        
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete '" + fileName + "'?",
            "Delete File", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (option != JOptionPane.YES_OPTION) {
            return;
        }
        
        String fileId = getFileIdForRow(selectedRow);
        if (fileId == null || fileId.trim().isEmpty()) {
            showError("File ID not found - unable to delete");
            return;
        }
        
        try {
            statusLabel.setText("Deleting file...");
            deleteButton.setEnabled(false);
            
            boolean success = client.getFileService().deleteFile(fileId, client.getUserId());
            
            if (success) {
                statusLabel.setText("File deleted successfully");
                // Remove from table
                tableModel.removeRow(selectedRow);
            } else {
                showError("Failed to delete file");
                statusLabel.setText("Delete failed");
            }
            
        } catch (Exception e) {
            showError("Delete error: " + e.getMessage());
            statusLabel.setText("Delete failed");
        } finally {
            deleteButton.setEnabled(false); // Will be re-enabled if another file is selected
        }
    }
    
    /**
     * Load shared files from server
     */
    private void loadSharedFiles() {
        if (!client.isConnected()) {
            return;
        }
        
        try {
            List<FileTransfer> files = client.getFileService().getSharedFiles();
            updateFileList(files);
        } catch (RemoteException e) {
            System.err.println("Failed to load shared files: " + e.getMessage());
        }
    }
    
    /**
     * Update the file list display
     */
    private void updateFileList(List<FileTransfer> files) {
        SwingUtilities.invokeLater(() -> {
            // Store the file list for ID tracking
            fileList.clear();
            fileList.addAll(files);
            
            tableModel.setRowCount(0);
            
            for (FileTransfer file : files) {
                System.out.println("Loading file: " + file.getFileName() + " with ID: " + file.getFileId());
                Object[] rowData = {
                    file.getFileName(),
                    file.getFormattedFileSize(),
                    file.getUploaderName(),
                    file.getFileType().toUpperCase()
                };
                tableModel.addRow(rowData);
            }
            
            statusLabel.setText("Files: " + files.size());
        });
    }
    
    /**
     * Add a shared file to the list
     */
    public void addSharedFile(FileTransfer fileMetadata) {
        SwingUtilities.invokeLater(() -> {
            // Check if file already exists in our list (prevent duplicates)
            String newFileId = fileMetadata.getFileId();
            for (FileTransfer existingFile : fileList) {
                if (existingFile.getFileId() != null && existingFile.getFileId().equals(newFileId)) {
                    // File already exists, don't add duplicate
                    return;
                }
            }
            
            // Add to our tracked list
            fileList.add(fileMetadata);
            
            Object[] rowData = {
                fileMetadata.getFileName(),
                fileMetadata.getFormattedFileSize(),
                fileMetadata.getUploaderName(),
                fileMetadata.getFileType().toUpperCase()
            };
            tableModel.addRow(rowData);
            
            statusLabel.setText("Files: " + tableModel.getRowCount());
        });
    }
    
    /**
     * Remove a shared file from the list
     */
    public void removeSharedFile(String fileId) {
        // Note: In a full implementation, you'd need to track file IDs
        // For now, we'll refresh the entire list
        SwingUtilities.invokeLater(() -> {
            loadSharedFiles();
        });
    }
    
    /**
     * Get file ID for a table row (now using actual stored file IDs)
     */
    private String getFileIdForRow(int row) {
        if (row >= 0 && row < fileList.size()) {
            return fileList.get(row).getFileId();
        }
        return null;
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "File Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Enable/disable file operations based on connection
     */
    public void setConnectionEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            uploadButton.setEnabled(enabled);
            
            if (!enabled) {
                downloadButton.setEnabled(false);
                deleteButton.setEnabled(false);
                statusLabel.setText("Disconnected");
                tableModel.setRowCount(0);
                fileList.clear(); // Clear tracked files
            } else {
                statusLabel.setText("Ready");
                loadSharedFiles();
            }
        });
    }
}
