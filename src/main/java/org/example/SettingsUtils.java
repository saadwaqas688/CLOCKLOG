package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SettingsUtils {

    // Database URL, username, and password
    // Replace with your database's actual credentials
    private static final String DB_URL = DatabaseConfig.getDatabaseUrl();

    // Save or update settings to the database
    public static void saveUserSettings(int userId, boolean autoStart, boolean hideDesktopApp,
                                        boolean systemTray, boolean taskBarAndSystemTray,
                                        boolean screenshotNotification, int reminderInterval,
                                        String timeZone) {

        // Query to update settings for an existing user
        String query = "UPDATE UserSettings SET auto_start = ?, hide_desktop_app = ?, system_tray = ?, " +
                "task_bar_and_system_tray = ?, screen_shot_notification = ?, time_tracking_reminder_interval = ?, " +
                "time_zone = ? WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setBoolean(1, autoStart);
            pstmt.setBoolean(2, hideDesktopApp);
            pstmt.setBoolean(3, systemTray);
            pstmt.setBoolean(4, taskBarAndSystemTray);
            pstmt.setBoolean(5, screenshotNotification);
            pstmt.setInt(6, reminderInterval);
            pstmt.setString(7, timeZone);
            pstmt.setInt(8, userId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Settings updated for user ID: " + userId + ". Rows affected: " + rowsAffected);
            } else {
                System.out.println("No settings found to update for user ID: " + userId);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
