package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;

public class SeedingDataBaseTwo {

    //    public static void main(String[] args) {
//        Connection conn = null;
//
//        try {
//            // Connect to the SQLite database (or create it if it doesn't exist)
//            String url = "jdbc:sqlite:app.db";
//            conn = DriverManager.getConnection(url);
//
//            if (conn != null) {
//                // Create tables
//                createTables(conn);
//
//                // Insert data
//                insertData(conn);
//
//                System.out.println("Database setup completed successfully.");
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (conn != null) {
//                    conn.close();
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
    public void run() {
        Connection conn = null;

        try {
            // Connect to the SQLite database (or create it if it doesn't exist)
            String url = DatabaseConfig.getDatabaseUrl();
            Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection(url);

            if (conn != null) {
                // Create tables
                createTables(conn);

                // Insert data
                insertData(conn);

                System.out.println("Database setup completed successfully.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void createTables(Connection conn) throws Exception {
        String createTasksTable = "CREATE TABLE IF NOT EXISTS Tasks ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "description TEXT, "
                + "created_at TEXT NOT NULL, "
                + "updated_at TEXT NOT NULL, "
                + "project_id INTEGER, "
                + "FOREIGN KEY (project_id) REFERENCES Projects(id)"
                + ");";

        String createProjectsTable = "CREATE TABLE IF NOT EXISTS Projects ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "description TEXT"
                + ");";

        String createUsersTable = "CREATE TABLE IF NOT EXISTS Users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "first_name TEXT NOT NULL, "
                + "last_name TEXT NOT NULL, "
                + "email TEXT NOT NULL, "
                + "password TEXT NOT NULL, "
                + "role TEXT NOT NULL"
                + ");";

        String createUsersProjectsTable = "CREATE TABLE IF NOT EXISTS UsersProjects ("
                + "user_id INTEGER, "
                + "project_id INTEGER, "
                + "PRIMARY KEY (user_id, project_id), "
                + "FOREIGN KEY (user_id) REFERENCES Users(id), "
                + "FOREIGN KEY (project_id) REFERENCES Projects(id)"
                + ");";

        String createUserTasksTable = "CREATE TABLE IF NOT EXISTS UserTasks ("
                + "user_id INTEGER, "
                + "task_id INTEGER, "
                + "status TEXT, "
                + "time_stamp INTEGER, "
                + "is_running INTEGER, "
                + "time_spent INTEGER, "
                + "PRIMARY KEY (user_id, task_id), "
                + "FOREIGN KEY (user_id) REFERENCES Users(id), "
                + "FOREIGN KEY (task_id) REFERENCES Tasks(id)"
                + ");";

        String createUserTaskActivityTable = "CREATE TABLE IF NOT EXISTS UserTaskActivity ("
                + "user_id INTEGER, "
                + "task_id INTEGER, "
                + "mouse_moves INTEGER, "
                + "keys_pressed INTEGER, "
                + "resource_used TEXT, "
                + "screen_shots BLOB, "
                + "time_spent INTEGER, "
                + "PRIMARY KEY (user_id, task_id), "
                + "FOREIGN KEY (user_id) REFERENCES Users(id), "
                + "FOREIGN KEY (task_id) REFERENCES Tasks(id)"
                + ");";

        String createUserSettingsTable = "CREATE TABLE IF NOT EXISTS UserSettings ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "auto_start INTEGER, "
                + "hide_desktop_app INTEGER, "
                + "system_tray INTEGER, "
                + "task_bar_and_system_tray INTEGER, "
                + "screen_shot_notification INTEGER, "
                + "time_tracking_reminder_interval INTEGER, "
                + "time_zone TEXT, "
                + "language TEXT, "
                + "user_id INTEGER, "
                + "FOREIGN KEY (user_id) REFERENCES Users(id)"
                + ");";
        String createUserTimeTable = "CREATE TABLE IF NOT EXISTS UserTime ("
                + "user_id INTEGER PRIMARY KEY, "
                + "hours_worked_daily INTEGER, "
                + "FOREIGN KEY (user_id) REFERENCES Users(id)"
                + ");";

        Statement stmt = conn.createStatement();
        stmt.execute(createTasksTable);
        stmt.execute(createProjectsTable);
        stmt.execute(createUsersTable);
        stmt.execute(createUsersProjectsTable);
        stmt.execute(createUserTasksTable);
        stmt.execute(createUserTaskActivityTable);
        stmt.execute(createUserSettingsTable);
        stmt.execute(createUserTimeTable);
    }

    private static void insertData(Connection conn) throws Exception {
        String insertUser = "INSERT INTO Users (first_name, last_name, email, password, role) VALUES (?, ?, ?, ?, ?)";
        String insertProject = "INSERT INTO Projects (name, description) VALUES (?, ?)";
        String insertTask = "INSERT INTO Tasks (name, description, created_at, updated_at, project_id) VALUES (?, ?, ?, ?, ?)";
        String insertUserProject = "INSERT INTO UsersProjects (user_id, project_id) VALUES (?, ?)";
        String insertUserTask = "INSERT INTO UserTasks (user_id, task_id, status, time_stamp, is_running, time_spent) VALUES (?, ?, ?, ?, ?, ?)";

        // Insert users
        try (PreparedStatement pstmt = conn.prepareStatement(insertUser)) {
            pstmt.setString(1, "John");
            pstmt.setString(2, "Doe");
            pstmt.setString(3, "u1");
            pstmt.setString(4, "p1");
            pstmt.setString(5, "employee");
            pstmt.executeUpdate();

            pstmt.setString(1, "Jane");
            pstmt.setString(2, "Smith");
            pstmt.setString(3, "u2");
            pstmt.setString(4, "p2");
            pstmt.setString(5, "manager");
            pstmt.executeUpdate();

            pstmt.setString(1, "Bob");
            pstmt.setString(2, "Johnson");
            pstmt.setString(3, "u3");
            pstmt.setString(4, "p3");
            pstmt.setString(5, "admin");
            pstmt.executeUpdate();
        }

        // Insert projects
        try (PreparedStatement pstmt = conn.prepareStatement(insertProject)) {
            for (int i = 1; i <= 10; i++) {
                pstmt.setString(1, "Project " + i);
                pstmt.setString(2, "Description for project " + i);
                pstmt.executeUpdate();
            }
        }

        // Insert tasks
        try (PreparedStatement pstmt = conn.prepareStatement(insertTask)) {
            for (int projectId = 1; projectId <= 10; projectId++) {
                for (int i = 1; i <= 5; i++) {
                    pstmt.setString(1, "Task " + i + " for Project " + projectId);
                    pstmt.setString(2, "Description for task " + i + " in project " + projectId);
                    pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
                    pstmt.setInt(5, projectId);
                    pstmt.executeUpdate();
                }
            }
        }

        // Assign first 4 projects to the first user
        try (PreparedStatement pstmt = conn.prepareStatement(insertUserProject)) {
            for (int i = 1; i <= 4; i++) {
                pstmt.setInt(1, 1);
                pstmt.setInt(2, i);
                pstmt.executeUpdate();
            }
        }

        // Assign remaining 6 projects to the second and third users
        try (PreparedStatement pstmt = conn.prepareStatement(insertUserProject)) {
            for (int i = 5; i <= 7; i++) {
                pstmt.setInt(1, 2);
                pstmt.setInt(2, i);
                pstmt.executeUpdate();
            }
            for (int i = 8; i <= 10; i++) {
                pstmt.setInt(1, 3);
                pstmt.setInt(2, i);
                pstmt.executeUpdate();
            }
        }

        // Assign tasks to users
        try (PreparedStatement pstmt = conn.prepareStatement(insertUserTask)) {
            for (int i = 1; i <= 20; i++) {
                pstmt.setInt(1, 1);
                pstmt.setInt(2, i);
                pstmt.setString(3, "ToDo");
                pstmt.setInt(4, 0);
                pstmt.setInt(5, 0);  // Start first task of each project
                pstmt.setInt(6, 0);
                pstmt.executeUpdate();
            }
            for (int i = 21; i <= 35; i++) {
                pstmt.setInt(1, 2);
                pstmt.setInt(2, i);
                pstmt.setString(3, "ToDo");
                pstmt.setInt(4, 0);
                pstmt.setInt(5, 0);
                pstmt.setInt(6, 0);
                pstmt.executeUpdate();
            }
            for (int i = 36; i <= 50; i++) {
                pstmt.setInt(1, 3);
                pstmt.setInt(2, i);
                pstmt.setString(3, "ToDo");
                pstmt.setInt(4, 0);
                pstmt.setInt(5, 0);
                pstmt.setInt(6, 0);
                pstmt.executeUpdate();
            }
        }

        // Insert user settings
        String insertUserSettings = "INSERT INTO UserSettings (auto_start, hide_desktop_app, system_tray, task_bar_and_system_tray, screen_shot_notification, time_tracking_reminder_interval, time_zone, language, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertUserSettings)) {
            for (int userId = 1; userId <= 3; userId++) {
                pstmt.setBoolean(1, true);  // auto_start
                pstmt.setBoolean(2, true);  // hide_desktop_app
                pstmt.setBoolean(3, true);  // system_tray
                pstmt.setBoolean(4, true);  // task_bar_and_system_tray
                pstmt.setBoolean(5, true);  // screen_shot_notification
                pstmt.setInt(6, 5);  // time_tracking_reminder_interval
                pstmt.setString(7, "(UTC+05:00) Islamabad, Karachi");  // time_zone
                pstmt.setInt(9, userId);  // user_id
                pstmt.executeUpdate();
            }
        }

        // Insert user time

        String insertUserTime = "INSERT INTO UserTime (user_id, hours_worked_daily) VALUES (?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertUserTime)) {
            for (int userId = 1; userId <= 3; userId++) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, 0);  // hours_worked_daily
                pstmt.executeUpdate();
            }
        }
    }
}
