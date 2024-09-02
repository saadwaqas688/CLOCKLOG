package org.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public class DatabaseConfig {

    private static final String APP_FOLDER_NAME = ".myapp"; // A hidden folder in the user's home directory

    // Method to get the database URL
    public static String getDatabaseUrl() {
        Path dbFilePath = getBaseDirectory().resolve("app.db").toAbsolutePath();
        System.out.println("Database Path: " + dbFilePath.toString()); // Debug print
        return "jdbc:sqlite:" + dbFilePath.toString();
    }

    // Method to get the log file path
    public static String getLogFilePath() {
        Path logFilePath = getBaseDirectory().resolve("user_activity.log").toAbsolutePath();
        System.out.println("Log File Path: " + logFilePath.toString()); // Debug print
        return logFilePath.toString();
    }


    // Method to get the log file path
    public static String getUrlLogFilePath() {
        Path logFilePath = getBaseDirectory().resolve("url.log").toAbsolutePath();
        System.out.println("url log file path: " + logFilePath.toString()); // Debug print
        return logFilePath.toString();
    }

    // Method to get the screenshot directory path
    public static String getScreenshotDirPath() {
        Path screenshotDir = getBaseDirectory().resolve("screenshots").toAbsolutePath();
        System.out.println("Screenshot Directory Path: " + screenshotDir.toString()); // Debug print
        createDirectoryIfNotExists(screenshotDir);
        return screenshotDir.toString();
    }

    // Helper method to get the base directory for the application
    private static Path getBaseDirectory() {
        String userHome = System.getProperty("user.home");
        System.out.println("User Home: " + userHome); // Debug print
        Path baseDirectory = Paths.get(userHome, APP_FOLDER_NAME);
        createDirectoryIfNotExists(baseDirectory);
        return baseDirectory;
    }

    public static Path getDatabaseFilePath() {
        // Return the absolute path of the database file
        return getBaseDirectory().resolve("app.db").toAbsolutePath();
    }


    // Helper method to create a directory if it doesn't exist
    private static void createDirectoryIfNotExists(Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
