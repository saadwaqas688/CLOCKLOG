//package org.example;
//
//import javax.swing.*;
//
//public class Main {
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            Login loginForm = new Login();
//            loginForm.setVisible(true);
//        });
//    }
//}
package org.example;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.*;

public class Main {
    private static final String DB_URL = DatabaseConfig.getDatabaseUrl();;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            if (!checkDatabaseAndTables()) {
                // Run the SeedingDataBaseTwo class if database or tables are missing
                SeedingDataBaseTwo seedDatabase = new SeedingDataBaseTwo();
                seedDatabase.run(); // Assuming 'run' method initiates the seeding process
            }
            Login loginForm = new Login();
            loginForm.setVisible(true);
        });
    }
    private static boolean checkDatabaseAndTables() {
        // Get the actual file path of the database
        Path dbFilePath = DatabaseConfig.getDatabaseFilePath();

        // Check if the database file exists
        if (!dbFilePath.toFile().exists()) {
            return false; // Database file does not exist
        }

        // Get the JDBC URL for connecting to the database
        String dbUrl = DatabaseConfig.getDatabaseUrl();

        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {

            // Check if tables exist
            boolean usersTableExists = tableExists(stmt, "Users");
            boolean tasksTableExists = tableExists(stmt, "Tasks");
            boolean userTasksTableExists = tableExists(stmt, "UserTasks");

            return usersTableExists && tasksTableExists && userTasksTableExists;

        } catch (SQLException e) {
            System.err.println("Error checking database and tables: " + e.getMessage());
            e.printStackTrace();
            return false; // Error occurred while checking tables
        }
    }

    // private static boolean checkDatabaseAndTables() {
    //     Path dbFilePath = DatabaseConfig.getDatabaseUrl();
    //     if (!dbFilePath.toFile().exists()) {
    //         return false; // Database file does not exist
    //     }

    //     try (Connection conn = DriverManager.getConnection(DB_URL);
    //          Statement stmt = conn.createStatement()) {

    //         // Check if tables exist
    //         boolean usersTableExists = tableExists(stmt, "Users");
    //         boolean tasksTableExists = tableExists(stmt, "Tasks");
    //         boolean userTasksTableExists = tableExists(stmt, "UserTasks");

    //         return usersTableExists && tasksTableExists && userTasksTableExists;

    //     } catch (SQLException e) {
    //         System.err.println("Error checking database and tables: " + e.getMessage());
    //         e.printStackTrace();
    //         return false; // Error occurred while checking tables
    //     }
    // }
//    private static boolean checkDatabaseAndTables() {
//        File dbFile = new File("app.db");
//        if (!dbFile.exists()) {
//            return false; // Database file does not exist
//        }
//
//        try (Connection conn = DriverManager.getConnection(DB_URL);
//             Statement stmt = conn.createStatement()) {
//
//            // Check if tables exist
//            boolean usersTableExists = tableExists(stmt, "Users");
//            boolean tasksTableExists = tableExists(stmt, "Tasks");
//            boolean userTasksTableExists = tableExists(stmt, "UserTasks");
//
//            return usersTableExists && tasksTableExists && userTasksTableExists;
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false; // Error occurred while checking tables
//        }
//    }

    private static boolean tableExists(Statement stmt, String tableName) throws SQLException {
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
        try (ResultSet rs = stmt.executeQuery(query)) {
            return rs.next(); // If result set is not empty, table exists
        }
    }
}
