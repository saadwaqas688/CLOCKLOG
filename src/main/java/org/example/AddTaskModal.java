package org.example;

import org.json.JSONObject;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class AddTaskModal extends JDialog {
    private static final Color SECONDARY_COLOR = new Color(0, 36, 51);
    private static final Color PRIMARY_COLOR = new Color(3, 50, 70);
    private static final Color PRIMARY_TEXT_COLOR = new Color(255, 255, 255);
    private static final Color WARNING_COLOR = new Color(244, 162, 97);

    private JTextField taskNameField;
    private JTextArea descriptionArea;
    private JComboBox<String> statusDropdown;
    private int projectId;

    private int userId;
    private String status;
    private Task createdTask;
    private static final String DB_URL = "jdbc:sqlite:app.db";


    public AddTaskModal(Frame owner, int projectId,int userId) {
        super(owner, "Add New Task", true);
        this.projectId = projectId;
        this.userId = userId;
        this.status = "Failed";
        this.createdTask = null;

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(SECONDARY_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;

        // Task Name Field
        JLabel taskNameLabel = new JLabel("Task Name:");
        taskNameLabel.setForeground(PRIMARY_TEXT_COLOR);
        gbc.gridy = 0;
        mainPanel.add(taskNameLabel, gbc);

        taskNameField = new JTextField();
        taskNameField.setBackground(PRIMARY_COLOR);
        taskNameField.setForeground(PRIMARY_TEXT_COLOR);
        taskNameField.setBorder(createPaddingBorder(10, 10, 10, 10));
        taskNameField.setPreferredSize(new Dimension(300, 50));
        gbc.gridy = 1;
        mainPanel.add(taskNameField, gbc);

        // Description Field
        JLabel descriptionLabel = new JLabel("Description:");
        descriptionLabel.setForeground(PRIMARY_TEXT_COLOR);
        gbc.gridy = 2;
        mainPanel.add(descriptionLabel, gbc);

        descriptionArea = new JTextArea();
        descriptionArea.setBackground(PRIMARY_COLOR);
        descriptionArea.setForeground(PRIMARY_TEXT_COLOR);
        descriptionArea.setBorder(createPaddingBorder(10, 10, 10, 10));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setPreferredSize(new Dimension(300, 100));
        gbc.gridy = 3;
        mainPanel.add(new JScrollPane(descriptionArea), gbc);

        // Status Dropdown
        JLabel statusLabel = new JLabel("Select Status:");
        statusLabel.setForeground(PRIMARY_TEXT_COLOR);
        gbc.gridy = 4;
        mainPanel.add(statusLabel, gbc);

        String[] statusOptions = {"Select", "Completed", "ToDo","InProgress","Review"};
        statusDropdown = new JComboBox<>(statusOptions);
        statusDropdown.setBackground(PRIMARY_COLOR);
        statusDropdown.setForeground(PRIMARY_TEXT_COLOR);
        statusDropdown.setBorder(BorderFactory.createEmptyBorder());
        statusDropdown.setPreferredSize(new Dimension(300, 50));
        statusDropdown.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel renderer = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                renderer.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
                renderer.setForeground(PRIMARY_TEXT_COLOR);
                renderer.setBackground(PRIMARY_COLOR);
                return renderer;
            }
        });
        gbc.gridy = 5;
        mainPanel.add(statusDropdown, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(SECONDARY_COLOR);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(PRIMARY_COLOR);
        cancelButton.setForeground(PRIMARY_TEXT_COLOR);
        cancelButton.setOpaque(true);
        cancelButton.setBorder(BorderFactory.createEmptyBorder());
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> dispose());

        JButton addButton = new JButton("Add");
        addButton.setBackground(WARNING_COLOR);
        addButton.setForeground(PRIMARY_TEXT_COLOR);
        addButton.setOpaque(true);
        addButton.setBorder(BorderFactory.createEmptyBorder());
        addButton.setPreferredSize(new Dimension(100, 35));
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddButtonClick();
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);

        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.EAST;
        mainPanel.add(buttonPanel, gbc);

        getContentPane().add(mainPanel);
        setSize(400, 500);
        setLocationRelativeTo(owner);
    }

    // Create a padding border
    private Border createPaddingBorder(int top, int left, int bottom, int right) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(top, left, bottom, right),
                BorderFactory.createLineBorder(new Color(0, 0, 0, 0), 1)
        );
    }

    // Handle the Add button click
//    private void handleAddButtonClick() {
//        String taskName = taskNameField.getText().trim();
//        String description = descriptionArea.getText().trim();
//        String status = (String) statusDropdown.getSelectedItem();
//
//        if (taskName.isEmpty() || "Select".equals(status)) {
//            JOptionPane.showMessageDialog(this, "Please fill out all fields correctly.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        // Construct the JSON payload
//        String jsonPayload = String.format("{\"projectId\":\"%s\", \"name\":\"%s\", \"description\":\"%s\", \"status\":\"%s\"}",
//                projectId, taskName, description, status);
//
//        // Send the POST request
//        String apiUrl = "http://localhost:3000/addTask";
//        JSONObject response = HttpUtil.sendPostRequest(apiUrl, jsonPayload);
//
//        // Handle the response to extract the created task
//        Task newTask = null;
//        if (response != null && response.has("success") && response.getBoolean("success")) {
//            try {
//                if (response.has("task")) {
//                    JSONObject taskJson = response.getJSONObject("task");
//                    newTask = parseTaskFromJson(taskJson);
//                    this.status = "Success";
//                    createdTask = newTask;
//                    JOptionPane.showMessageDialog(this, "Task added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
//                    dispose();
//                }
//            } catch (Exception e) {
//                this.status = "Failed";
//                JOptionPane.showMessageDialog(this, "Failed to parse task from response.", "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        } else {
//            this.status = "Failed";
//            JOptionPane.showMessageDialog(this, "Failed to add task.", "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }

//    private void handleAddButtonClick() {
//        String taskName = taskNameField.getText().trim();
//        String description = descriptionArea.getText().trim();
//        String status = (String) statusDropdown.getSelectedItem();
//        int userId = 1; // Assuming userId is 1 as per your requirement
//
//        if (taskName.isEmpty() || "Select".equals(status)) {
//            JOptionPane.showMessageDialog(this, "Please fill out all fields correctly.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        try (Connection conn = DriverManager.getConnection(DB_URL)) {
//            conn.setAutoCommit(false);
//
//            LocalDateTime currentDateTime = LocalDateTime.now();
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//            String formattedDateTime = currentDateTime.format(formatter);
//
//            String insertTaskSQL = "INSERT INTO Tasks (project_id, name, description, created_at, updated_at) " +
//                    "VALUES (?, ?, ?, ?, ?)";
//            try (PreparedStatement pstmt = conn.prepareStatement(insertTaskSQL, Statement.RETURN_GENERATED_KEYS)) {
//                pstmt.setInt(1, projectId);
//                pstmt.setString(2, taskName);
//                pstmt.setString(3, description);
//                pstmt.setString(4, formattedDateTime); // created_at
//                pstmt.setString(5, formattedDateTime); // updated_at
//
//                int affectedRows = pstmt.executeUpdate();
//                if (affectedRows == 0) {
//                    throw new SQLException("Creating task failed, no rows affected.");
//                }
//
//                try (var generatedKeys = pstmt.getGeneratedKeys()) {
//                    if (generatedKeys.next()) {
//                        long taskId = generatedKeys.getLong(1);
//
//                        String insertUserTaskSQL = "INSERT INTO UserTasks (user_id, task_id, status, time_stamp, is_running, time_spent) " +
//                                "VALUES (?, ?, ?, ?, 0, 0)";
//                        try (PreparedStatement pstmtUserTask = conn.prepareStatement(insertUserTaskSQL)) {
//                            pstmtUserTask.setInt(1, userId);
//                            pstmtUserTask.setLong(2, taskId);
//                            pstmtUserTask.setString(3, status);
//                            pstmtUserTask.setString(4, formattedDateTime); // time_stamp
//                            pstmtUserTask.executeUpdate();
//                        }
//                        this.status = "Success";
//                        Task newTask = new Task(
//                                projectId,
//                                String.valueOf(taskId),
//                                taskName,
//                                description,
//                                formattedDateTime,
//                                status,
//                                0
//                        );
//
//                        createdTask = newTask;
//                    } else {
//                        throw new SQLException("Creating task failed, no ID obtained.");
//                    }
//                }
//
//                conn.commit();
//                JOptionPane.showMessageDialog(this, "Task added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
//                dispose();
//            } catch (SQLException ex) {
//                conn.rollback();
//                throw ex;
//            }
//        } catch (SQLException e) {
//            this.status = "Failed";
//            JOptionPane.showMessageDialog(this, "Failed to add task: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
private void handleAddButtonClick() {
    String taskName = taskNameField.getText().trim();
    String description = descriptionArea.getText().trim();
    String status = (String) statusDropdown.getSelectedItem();
    int userId = 1; // Assuming userId is 1 as per your requirement

    if (taskName.isEmpty() || "Select".equals(status)) {
        JOptionPane.showMessageDialog(this, "Please fill out all fields correctly.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    Connection conn = null;
    try {
        // Load the SQLite JDBC driver
        Class.forName("org.sqlite.JDBC");

        // Get the database URL from the DatabaseConfig class
        String url = DatabaseConfig.getDatabaseUrl();

        // Establish a connection to the database
        conn = DriverManager.getConnection(url);
        conn.setAutoCommit(false);

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(formatter);

        String insertTaskSQL = "INSERT INTO Tasks (project_id, name, description, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertTaskSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, projectId);
            pstmt.setString(2, taskName);
            pstmt.setString(3, description);
            pstmt.setString(4, formattedDate); // created_at (only date)
            pstmt.setString(5, formattedDate); // updated_at (only date)

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }

            try (var generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long taskId = generatedKeys.getLong(1);

                    String insertUserTaskSQL = "INSERT INTO UserTasks (user_id, task_id, status, time_stamp, is_running, time_spent) " +
                            "VALUES (?, ?, ?, ?, 0, 0)";
                    try (PreparedStatement pstmtUserTask = conn.prepareStatement(insertUserTaskSQL)) {
                        pstmtUserTask.setInt(1, userId);
                        pstmtUserTask.setLong(2, taskId);
                        pstmtUserTask.setString(3, status);
                        pstmtUserTask.setString(4, formattedDate); // time_stamp (only date)
                        pstmtUserTask.executeUpdate();
                    }
                    this.status = "Success";
                    Task newTask = new Task(
                            projectId,
                            String.valueOf(taskId),
                            taskName,
                            description,
                            formattedDate,  // Pass only the date
                            status,
                            0
                    );

                    createdTask = newTask;
                } else {
                    throw new SQLException("Creating task failed, no ID obtained.");
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Task added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException ex) {
            if (conn != null) {
                conn.rollback();
            }
            throw ex;
        }
    } catch (SQLException | ClassNotFoundException e) {
        this.status = "Failed";
        JOptionPane.showMessageDialog(this, "Failed to add task: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } finally {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to close database connection: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}


//    private void handleAddButtonClick() {
//        String taskName = taskNameField.getText().trim();
//        String description = descriptionArea.getText().trim();
//        String status = (String) statusDropdown.getSelectedItem();
//        int userId = 1; // Assuming userId is 1 as per your requirement
//
//        if (taskName.isEmpty() || "Select".equals(status)) {
//            JOptionPane.showMessageDialog(this, "Please fill out all fields correctly.", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//
//        try (Connection conn = DriverManager.getConnection(DB_URL)) {
//            conn.setAutoCommit(false);
//
//            LocalDate currentDate = LocalDate.now();
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            String formattedDate = currentDate.format(formatter);
//
//            String insertTaskSQL = "INSERT INTO Tasks (projectId, name, description, createDate, timeStamp, isRunning, timeSpent) " +
//                    "VALUES (?, ?, ?, ?, ?, ?, 0, 0)";
//            try (PreparedStatement pstmt = conn.prepareStatement(insertTaskSQL, Statement.RETURN_GENERATED_KEYS)) {
//                pstmt.setInt(1, projectId);
//                pstmt.setString(2, taskName);
//                pstmt.setString(3, description);
//                pstmt.setString(4, formattedDate); // createDate
//                pstmt.setString(5, status);
//                pstmt.setString(6, formattedDate); // timeStamp
//
//                int affectedRows = pstmt.executeUpdate();
//                if (affectedRows == 0) {
//                    throw new SQLException("Creating task failed, no rows affected.");
//                }
//
//                try (var generatedKeys = pstmt.getGeneratedKeys()) {
//                    if (generatedKeys.next()) {
//                        long taskId = generatedKeys.getLong(1);
//
//                        String insertUserTaskSQL = "INSERT INTO userTasks (userId, taskId) VALUES (?, ?)";
//                        try (PreparedStatement pstmtUserTask = conn.prepareStatement(insertUserTaskSQL)) {
//                            pstmtUserTask.setInt(1, userId);
//                            pstmtUserTask.setLong(2, taskId);
//                            pstmtUserTask.executeUpdate();
//                        }
//                        this.status = "Success";
//                        Task newTask = new Task(
//                                projectId,
//                                String.valueOf(taskId),
//                                taskName,
//                                description,
//                                formattedDate,
//                                status,
//                                0
//                        );
//
//                        createdTask = newTask;
//                    } else {
//                        throw new SQLException("Creating task failed, no ID obtained.");
//                    }
//                }
//
//                conn.commit();
//                JOptionPane.showMessageDialog(this, "Task added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
//                dispose();
//            } catch (SQLException ex) {
//                conn.rollback();
//                throw ex;
//            }
//        } catch (SQLException e) {
//            this.status = "Failed";
//            JOptionPane.showMessageDialog(this, "Failed to add task: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }

    // Parse task from JSON response
    private Task parseTaskFromJson(JSONObject json) {
        try {
            int projectId = json.optInt("projectId", 0);
            String id = json.optString("id", null);
            String name = json.optString("name", null);
            String description = json.optString("description", null);
            String createDate = json.optString("createDate", null);
            String status = json.optString("status", null);
            int timeSpent = json.optInt("timeSpent", 0); // Default value if missing

            if (projectId == 0 || id == null || name == null || description == null || createDate == null || status == null) {
                throw new IllegalArgumentException("Missing required fields in JSON");
            }

            return new Task(projectId, id, name, description, createDate, status, timeSpent);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getStatus() {
        return status;
    }

    public Task getCreatedTask() {
        return createdTask;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AddTaskModal dialog = new AddTaskModal(null, 3,3);
            dialog.setVisible(true);
        });
    }
}
