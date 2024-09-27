package com.example;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
public class DownloadController {
    
        @FXML
        private TextField urlField;
        @FXML
        private Button selectDirectoryButton;
        @FXML
        private Label downloadDirectoryLabel;
        @FXML
        private Button startButton;
        @FXML
        private Button pauseButton;
        @FXML
        private Button resumeButton;
        @FXML
        private ProgressBar progressBar;
        @FXML
        private Label downloadSpeedLabel;
        @FXML
        private Label estimatedTimeLabel;
        @FXML
        private TableView<DownloadHistory> historyTable;
        @FXML
        private TableColumn<DownloadHistory, String> urlColumn;
        @FXML
        private TableColumn<DownloadHistory, String> progressColumn;
    
        private DownloadManager downloadManager;
        private String downloadDirectory = System.getProperty("user.home"); // Default download directory
    
        public void initialize() {
            downloadManager = new DownloadManager(this);
            setupTable();
            updateDownloadDirectoryLabel();
        }
    
        private void setupTable() {
            urlColumn.setCellValueFactory(cellData -> cellData.getValue().urlProperty());
            progressColumn.setCellValueFactory(cellData -> cellData.getValue().progressProperty());
        }
    
        @FXML
        public void selectDownloadDirectory() {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Download Directory");
            File selectedDirectory = directoryChooser.showDialog(selectDirectoryButton.getScene().getWindow());
            
            if (selectedDirectory != null) {
                downloadDirectory = selectedDirectory.getAbsolutePath();
                updateDownloadDirectoryLabel();
            }
        }
    
        private void updateDownloadDirectoryLabel() {
            downloadDirectoryLabel.setText("Download Directory: " + downloadDirectory);
        }
    
        @FXML
        public void startDownload() {
            String url = urlField.getText();
            String filePath = downloadDirectory + File.separator + url.substring(url.lastIndexOf('/') + 1);
            downloadManager.startDownload(url, filePath);
            urlField.clear();
        }
    
        @FXML
        public void pauseDownload() {
            downloadManager.pauseDownload();
        }
    
        @FXML
        public void resumeDownload() {
            String url = urlField.getText();
            String filePath = downloadDirectory + File.separator + url.substring(url.lastIndexOf('/') + 1);
            downloadManager.resumeDownload(url, filePath);
        }
    
        public void updateProgress(double progress, String speed, String estimatedTime) {
            Platform.runLater(() -> {
                progressBar.setProgress(progress);
                downloadSpeedLabel.setText("Speed: " + speed);
                estimatedTimeLabel.setText("Estimated Time: " + estimatedTime);
            });
        }
    
        public void showNotification(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Download Status");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
      

