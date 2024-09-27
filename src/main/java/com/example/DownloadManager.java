package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DownloadManager {
    private boolean isPaused = false;
    private final DownloadController controller;

    public DownloadManager(DownloadController controller) {
        this.controller = controller;
    }

    public void startDownload(String fileURL, String saveFilePath) {
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(fileURL).openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    controller.showNotification("Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
                    return;
                }

                int fileSize = connection.getContentLength();
                saveDownloadToDatabase(fileURL, saveFilePath, fileSize);
                
                InputStream inputStream = connection.getInputStream();
                RandomAccessFile outputFile = new RandomAccessFile(saveFilePath, "rw");

                byte[] buffer = new byte[1024];
                int bytesRead;
                int downloadedSize = 0;
                long startTime = System.currentTimeMillis();

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    if (isPaused) {
                        break; 
                    }
                    outputFile.write(buffer, 0, bytesRead);
                    downloadedSize += bytesRead;

                    updateDownloadProgressInDatabase(fileURL, downloadedSize);

                    long elapsedTime = System.currentTimeMillis() - startTime;
                    double speed = (downloadedSize / 1024.0) / (elapsedTime / 1000.0); // KB/s
                    double remainingSize = fileSize - downloadedSize;
                    String estimatedTime = (remainingSize > 0) ? String.format("%.1f seconds", remainingSize / (speed * 1024)) : "Completed";

                
                    controller.updateProgress((double) downloadedSize / fileSize, String.format("%.2f KB/s", speed), estimatedTime);
                }

                outputFile.close();
                inputStream.close();

                completeDownloadInDatabase(fileURL);
                controller.showNotification("Download completed successfully.");
            } catch (IOException | SQLException e) {
                controller.showNotification("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public void pauseDownload() {
        isPaused = true;
    }

    public void resumeDownload(String fileURL, String saveFilePath) {
        isPaused = false;
        startDownload(fileURL, saveFilePath);
    }

    private void saveDownloadToDatabase(String url, String filePath, int totalSize) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/download_manager", "username", "password")) {
            String query = "INSERT INTO download_history (url, file_path, total_size, downloaded_size, status) VALUES (?, ?, ?, 0, 'in_progress')";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, url);
            stmt.setString(2, filePath);
            stmt.setInt(3, totalSize);
            stmt.executeUpdate();
        }
    }

    private void updateDownloadProgressInDatabase(String url, int downloadedSize) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/download_manager", "username", "password")) {
            String query = "UPDATE download_history SET downloaded_size = ? WHERE url = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, downloadedSize);
            stmt.setString(2, url);
            stmt.executeUpdate();
        }
    }

    private void completeDownloadInDatabase(String url) throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/download_manager", "username", "password")) {
            String query = "UPDATE download_history SET status = 'completed' WHERE url = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, url);
            stmt.executeUpdate();
        }
    }
}
