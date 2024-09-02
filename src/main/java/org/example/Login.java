package org.example;

import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends JFrame {

    private static final Color SECONDARY_COLOR = new Color(0, 36, 51);
    private static final Color PRIMARY_COLOR = new Color(3, 50, 70);
    private static final Color PRIMARY_TEXT_COLOR = new Color(255, 255, 255);
    private static final Color WARNING_COLOR = new Color(244, 162, 97);

    private JTextField emailField;
    private JPasswordField passwordField;
    private static final String DB_URL = "jdbc:sqlite:app.db"; // Update with your database file path


    public Login() {
        setTitle("Login");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(SECONDARY_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;

        // Email Field
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(PRIMARY_TEXT_COLOR);
        gbc.gridy = 0;
        mainPanel.add(emailLabel, gbc);

        emailField = new JTextField();
        emailField.setBackground(PRIMARY_COLOR);
        emailField.setForeground(PRIMARY_TEXT_COLOR);
        emailField.setPreferredSize(new Dimension(300, 30));
        gbc.gridy = 1;
        mainPanel.add(emailField, gbc);

        // Password Field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(PRIMARY_TEXT_COLOR);
        gbc.gridy = 2;
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setBackground(PRIMARY_COLOR);
        passwordField.setForeground(PRIMARY_TEXT_COLOR);
        passwordField.setPreferredSize(new Dimension(300, 30));
        gbc.gridy = 3;
        mainPanel.add(passwordField, gbc);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(WARNING_COLOR);
        loginButton.setForeground(PRIMARY_TEXT_COLOR);
        loginButton.setOpaque(true);
        loginButton.setBorder(BorderFactory.createEmptyBorder());
        loginButton.setPreferredSize(new Dimension(100, 30));
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(loginButton, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        getContentPane().add(mainPanel);
    }
// Api call
//private void handleLogin() {
//    String email = emailField.getText().trim();
//    String password = new String(passwordField.getPassword()).trim();
//
//    if (email.isEmpty() || password.isEmpty()) {
//        JOptionPane.showMessageDialog(this, "Please fill out all fields.", "Error", JOptionPane.ERROR_MESSAGE);
//        return;
//    }
//
//    String jsonPayload = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);
//    String apiUrl = "http://localhost:3000/login";
//    JSONObject response = HttpUtil.sendPostRequest(apiUrl, jsonPayload);
//
//    if (response != null && response.has("success") && response.getBoolean("success")) {
//        User user = saveUserDetails(response.getJSONObject("user"));
//        JOptionPane.showMessageDialog(this, "Login successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
//
//        // Redirect to FolderList with only the User object
//        FolderList folderList = new FolderList(user);
//        folderList.setVisible(true);
//        this.dispose();
//    } else {
//        JOptionPane.showMessageDialog(this, "Invalid email or password.", "Error", JOptionPane.ERROR_MESSAGE);
//    }
//}

//    private void handleLogin() {
//        String email = emailField.getText().trim();
//        String password = new String(passwordField.getPassword()).trim();
//
//        if (email.isEmpty() || password.isEmpty()) {
//            JOptionPane.showMessageDialog(this, "Please fill out all fields.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        String jsonPayload = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);
//        String apiUrl = "http://localhost:3000/login";
//        JSONObject response = HttpUtil.sendPostRequest(apiUrl, jsonPayload);
//
//        if (response != null && response.has("success") && response.getBoolean("success")) {
//            saveUserDetails(response.getJSONObject("user"));
//            JOptionPane.showMessageDialog(this, "Login successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
//            // Redirect to FolderList
//            String selectedTaskId = "1";  // Replace with actual value
//            String selectedProjectId = "2";
//            boolean isDefaultSelectedTaskRunning=true;
//            boolean isGlobalTimerRunning=true;
//            FolderList folderList = new FolderList(selectedTaskId, selectedProjectId,isGlobalTimerRunning,isDefaultSelectedTaskRunning);
//            folderList.setVisible(true);
//            this.dispose();
//        } else {
//            JOptionPane.showMessageDialog(this, "Invalid email or password.", "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//private void handleLogin() {
//    String email = emailField.getText().trim();
//    String password = new String(passwordField.getPassword()).trim();
//
//    if (email.isEmpty() || password.isEmpty()) {
//        JOptionPane.showMessageDialog(this, "Please fill out all fields.", "Error", JOptionPane.ERROR_MESSAGE);
//        return;
//    }
//
//    String jsonPayload = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);
//    String apiUrl = "http://localhost:3000/users/login";
//    JSONObject response = HttpUtil.sendPostRequest(apiUrl, jsonPayload);
//    System.out.println("Error during authentication: "+ response);
//
//    if (response != null && response.has("success") && response.getBoolean("success")) {
//        User user = saveUserDetails(response.getJSONObject("user"));
//        JOptionPane.showMessageDialog(this, "Login successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
//
//        // Redirect to FolderList with only the User object
//        FolderList folderList = new FolderList(user);
//        folderList.setVisible(true);
//        this.dispose();
//    } else {
//        JOptionPane.showMessageDialog(this, "Invalid email or password.", "Error", JOptionPane.ERROR_MESSAGE);
//    }
//}
//
//    private User saveUserDetails(JSONObject userDetails) {
//        try {
//            // Extract user details from JSON
//            int userId = userDetails.getInt("userId");
//            String firstName = userDetails.getString("firstName");
//            String lastName = userDetails.getString("lastName");
//            String email = userDetails.getString("email");
//            String role = userDetails.getString("role");
//            boolean autoStart = userDetails.getBoolean("autoStart");
//            boolean hideDesktopApp = userDetails.getBoolean("hideDesktopApp");
//            boolean systemTray = userDetails.getBoolean("systemTray");
//            boolean taskBarAndSystemTray = userDetails.getBoolean("taskBarAndSystemTray");
//            boolean screenShotNotification = userDetails.getBoolean("screenShotNotification");
//            int timeTrackingReminderInterval = userDetails.getInt("timeTrackingReminderInterval");
//            String timeZone = userDetails.getString("timeZone");
//            String language = userDetails.getString("language");
//            int hoursWorkedDaily = userDetails.getInt("hoursWorkedDaily");
//            int taskId = userDetails.getInt("taskId");
//            String status = userDetails.getString("status");
//            int timeStamp = userDetails.getInt("timeStamp");
//            boolean isRunning = userDetails.getBoolean("isRunning");
//            int timeSpent = userDetails.getInt("timeSpent");
//
//            // Create User object
//            User user = new User(userId, firstName, lastName, email, role, autoStart, hideDesktopApp, systemTray, taskBarAndSystemTray, screenShotNotification, timeTrackingReminderInterval, timeZone, language, hoursWorkedDaily, taskId, status, timeStamp, isRunning, timeSpent);
//
//            // Save user details to SQLite database
//            saveUserToDatabase(user);
//
//            return user;
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Error processing user details");
//        }
//    }
//
//    private void saveUserToDatabase(User user) {
//        // JDBC URL for the SQLite database
//        String dbUrl = "jdbc:sqlite:app.db";
//
//        // SQL statement to insert user data
//        String insertUserSQL = "INSERT INTO User (" +
//                "userId, firstName, lastName, email, role, autoStart, " +
//                "hideDesktopApp, systemTray, taskBarAndSystemTray, " +
//                "screenShotNotification, timeTrackingReminderInterval, " +
//                "timeZone, language, hoursWorkedDaily, taskId, status, " +
//                "timeStamp, isRunning, timeSpent) " +
//                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//
//        try (Connection conn = DriverManager.getConnection(dbUrl);
//             PreparedStatement pstmt = conn.prepareStatement(insertUserSQL)) {
//
//            // Set values for the prepared statement
//            pstmt.setInt(1, user.getUserId());
//            pstmt.setString(2, user.getFirstName());
//            pstmt.setString(3, user.getLastName());
//            pstmt.setString(4, user.getEmail());
//            pstmt.setString(5, user.getRole());
//            pstmt.setBoolean(6, user.isAutoStart());
//            pstmt.setBoolean(7, user.isHideDesktopApp());
//            pstmt.setBoolean(8, user.isSystemTray());
//            pstmt.setBoolean(9, user.isTaskBarAndSystemTray());
//            pstmt.setBoolean(10, user.isScreenShotNotification());
//            pstmt.setInt(11, user.getTimeTrackingReminderInterval());
//            pstmt.setString(12, user.getTimeZone());
//            pstmt.setString(13, user.getLanguage());
//            pstmt.setInt(14, user.getHoursWorkedDaily());
//            pstmt.setInt(15, user.getTaskId());  // Assuming taskId is a property in the User class
//            pstmt.setString(16, user.getStatus());  // Assuming status is a property in the User class
//            pstmt.setInt(17, user.getTimeStamp());  // Assuming timeStamp is a property in the User class
//            pstmt.setBoolean(18, user.isRunning());  // Assuming isRunning is a property in the User class
//            pstmt.setInt(19, user.getTimeSpent());  // Assuming timeSpent is a property in the User class
//
//            // Execute the insertion
//            pstmt.executeUpdate();
//            System.out.println("User saved to database successfully.");
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            System.out.println("Failed to save user to the database.");
//        }
//    }



//    private void handleLogin() {
//        String email = emailField.getText().trim();
//        String password = new String(passwordField.getPassword()).trim();
//
//        if (email.isEmpty() || password.isEmpty()) {
//            JOptionPane.showMessageDialog(this, "Please fill out all fields.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        User user = authenticateUser(email, password);
//        if (user != null) {
//            JOptionPane.showMessageDialog(this, "Login successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
//            FolderList folderList = new FolderList(user);
//            folderList.setVisible(true);
//            this.dispose();
//        } else {
//            JOptionPane.showMessageDialog(this, "Invalid email or password.", "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    private User authenticateUser(String email, String password) {
//        String sql = "SELECT id, firstName, lastName, global_timer FROM users WHERE email = ? AND password = ?";
//
//        try (Connection conn = DriverManager.getConnection(DB_URL);
//             PreparedStatement pstmt = conn.prepareStatement(sql)) {
//
//            pstmt.setString(1, email);
//            pstmt.setString(2, password); // Make sure to hash passwords in a real application
//
//            try (ResultSet rs = pstmt.executeQuery()) {
//                if (rs.next()) {
//                    int id = rs.getInt("id");
//                    String firstName = rs.getString("firstName");
//                    String lastName = rs.getString("lastName");
//                    int timeSpent = rs.getInt("global_timer");
////                  return new User(id, firstName, lastName, timeSpent);
//                }
//            }
//
//        } catch (SQLException e) {
//            System.err.println("Error during authentication: " + e.getMessage());
//            e.printStackTrace();
//        }
//
//        return null;
//    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill out all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user;
        try {
            user = LoginUtils.getUserByEmail(email, password);
            JOptionPane.showMessageDialog(this, "Login successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
            FolderList folderList = new FolderList(user);
            folderList.setVisible(true);
            this.dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }






    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Login loginForm = new Login();
            loginForm.setVisible(true);
        });
    }
}
