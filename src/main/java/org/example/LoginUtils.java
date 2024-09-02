package org.example;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginUtils {

    public static User getUserByEmail(String email, String password) throws Exception {
        String url = DatabaseConfig.getDatabaseUrl(); // Adjust the database path as needed
        Class.forName("org.sqlite.JDBC");

        try (Connection conn = DriverManager.getConnection(url)) {

            // Fetch user details
            String userQuery = "SELECT id AS userId, first_name AS firstName, last_name AS lastName, email AS email, password AS password, role AS role FROM Users WHERE email = ?";
            try (PreparedStatement userStmt = conn.prepareStatement(userQuery)) {
                userStmt.setString(1, email);
                try (ResultSet userRs = userStmt.executeQuery()) {
                    if (!userRs.next()) {
                        throw new Exception("User not found");
                    }

                    String dbPassword = userRs.getString("password");
                    if (!dbPassword.equals(password)) {
                        throw new Exception("Incorrect password");
                    }

                    // Fetch user settings
                    String settingsQuery = "SELECT auto_start AS autoStart, hide_desktop_app AS hideDesktopApp, system_tray AS systemTray, task_bar_and_system_tray AS taskBarAndSystemTray, screen_shot_notification AS screenShotNotification, time_tracking_reminder_interval AS timeTrackingReminderInterval, time_zone AS timeZone FROM UserSettings WHERE user_id = ?";
                    boolean autoStart = false, hideDesktopApp = false, systemTray = false, taskBarAndSystemTray = false, screenShotNotification = false;
                    int timeTrackingReminderInterval = 0;
                    String timeZone = null;

                    try (PreparedStatement settingsStmt = conn.prepareStatement(settingsQuery)) {
                        settingsStmt.setInt(1, userRs.getInt("userId"));
                        try (ResultSet settingsRs = settingsStmt.executeQuery()) {
                            if (settingsRs.next()) {
                                autoStart = settingsRs.getBoolean("autoStart");
                                hideDesktopApp = settingsRs.getBoolean("hideDesktopApp");
                                systemTray = settingsRs.getBoolean("systemTray");
                                taskBarAndSystemTray = settingsRs.getBoolean("taskBarAndSystemTray");
                                screenShotNotification = settingsRs.getBoolean("screenShotNotification");
                                timeTrackingReminderInterval = settingsRs.getInt("timeTrackingReminderInterval");
                                timeZone = settingsRs.getString("timeZone");
                            }
                        }
                    }

                    // Fetch user time
                    String timeQuery = "SELECT hours_worked_daily AS hoursWorkedDaily FROM UserTime WHERE user_id = ?";
                    int hoursWorkedDaily = 0;
                    try (PreparedStatement timeStmt = conn.prepareStatement(timeQuery)) {
                        timeStmt.setInt(1, userRs.getInt("userId"));
                        try (ResultSet timeRs = timeStmt.executeQuery()) {
                            if (timeRs.next()) {
                                hoursWorkedDaily = timeRs.getInt("hoursWorkedDaily");
                            }
                        }
                    }

                    // Fetch running task
                    String taskQuery = "SELECT task_id AS taskId, status AS status, time_stamp AS timeStamp, is_running AS isRunning, time_spent AS timeSpent FROM UserTasks WHERE user_id = ? AND is_running = 1";
                    int taskId = 0, timeStamp = 0, timeSpent = 0;
                    String status = null;
                    boolean isRunning = false;

                    try (PreparedStatement taskStmt = conn.prepareStatement(taskQuery)) {
                        taskStmt.setInt(1, userRs.getInt("userId"));
                        try (ResultSet taskRs = taskStmt.executeQuery()) {
                            if (taskRs.next()) {
                                taskId = taskRs.getInt("taskId");
                                status = taskRs.getString("status");
                                timeStamp = taskRs.getInt("timeStamp");
                                isRunning = taskRs.getBoolean("isRunning");
                                timeSpent = taskRs.getInt("timeSpent");
                            }
                        }
                    }

                    // Populate and return User object
                    return new User(
                            userRs.getInt("userId"),
                            userRs.getString("firstName"),
                            userRs.getString("lastName"),
                            userRs.getString("email"),
                            userRs.getString("role"),
                            autoStart,
                            hideDesktopApp,
                            systemTray,
                            taskBarAndSystemTray,
                            screenShotNotification,
                            timeTrackingReminderInterval,
                            timeZone,
                            hoursWorkedDaily,
                            taskId,
                            status,
                            timeStamp,
                            isRunning,
                            timeSpent
                    );
                }
            }
        }
    }
}
