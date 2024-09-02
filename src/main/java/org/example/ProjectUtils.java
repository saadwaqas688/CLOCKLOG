package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectUtils {

    private static final String DB_URL = DatabaseConfig.getDatabaseUrl();; // Adjust the database path as needed

    public FetchProjectsResult fetchProjects(int userId) {
        List<Project> projectList = new ArrayList<>();
        Task runningTask = null; // Track the running task
        Project selectedProject = null;
        String sql = "SELECT p.id AS projectId, p.name AS projectName, p.description AS projectDescription, " +
                "t.id AS taskId, t.name AS taskName, t.description AS taskDescription, " +
                "t.created_at AS taskCreateDate, ut.status AS taskStatus, ut.time_spent AS timeSpent, ut.is_running AS isRunning " +
                "FROM Projects p " +
                "JOIN Tasks t ON p.id = t.project_id " +
                "JOIN UserTasks ut ON t.id = ut.task_id " +
                "WHERE ut.user_id = ?";

        System.out.println("Starting fetchProjects method.");

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            System.out.println("Executing query with userId: " + userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("No results found for the query.");
                } else {
                    System.out.println("Query returned results.");
                }

                Map<Integer, Project> projectMap = new HashMap<>();

                while (rs.next()) {
                    int projectId = rs.getInt("projectId");
                    String projectName = rs.getString("projectName");
                    String projectDescription = rs.getString("projectDescription");
                    String taskId = rs.getString("taskId");
                    String taskName = rs.getString("taskName");
                    String taskDescription = rs.getString("taskDescription");
                    long taskCreateDateMillis = rs.getLong("taskCreateDate");
                    String taskStatus = rs.getString("taskStatus");
                    int timeSpent = rs.getInt("timeSpent");
                    boolean isRunning = rs.getBoolean("isRunning");

                    // Convert milliseconds to LocalDate
                    Instant instant = Instant.ofEpochMilli(taskCreateDateMillis);
                    LocalDate parsedDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

                    System.out.println("Processing projectId: " + projectId);
                    System.out.println("Project Name: " + projectName);
                    System.out.println("Task ID: " + taskId);
                    System.out.println("Task Name: " + taskName);
                    System.out.println("Task Description: " + taskDescription);
                    System.out.println("Task Create Date: " + parsedDate);
                    System.out.println("Task Status: " + taskStatus);
                    System.out.println("Task Duration: " + timeSpent);

                    // Fetch the project or create a new one if it does not exist
                    Project project = projectMap.computeIfAbsent(projectId, id ->
                            new Project(id, projectName, projectDescription, new ArrayList<>())
                    );

                    if (project != null) {
                        Task task = new Task(
                                projectId,  // Assuming projectId is needed in Task
                                taskId,
                                taskName,
                                taskDescription,
                                parsedDate.toString(),
                                taskStatus,
                                timeSpent
                        );

                        if (isRunning) {
                            System.out.println("Task is running: " + taskId);
                            runningTask = task; // Update the running task
                            selectedProject = project; // Update the project of the running task
                        }

                        System.out.println("Adding task to project: " + taskId);

                        project.getTasks().add(task);
                    }
                }

                if (runningTask != null) {
                    System.out.println("Found running task: " + runningTask.getId());
                    System.out.println("Selected project: " + selectedProject.getId());
                } else {
                    System.out.println("No running tasks found.");
                }

                System.out.println("Adding projects to the list.");
                projectList.addAll(projectMap.values());
            } catch (SQLException e) {
                System.err.println("Error executing query: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("fetchProjects method completed.");
        return new FetchProjectsResult(projectList, runningTask, selectedProject);
    }

    // Static inner class to hold fetch result data
    public static class FetchProjectsResult {
        private final List<Project> projectList;
        private final Task runningTask;
        private final Project selectedProject;

        public FetchProjectsResult(List<Project> projectList, Task runningTask, Project selectedProject) {
            this.projectList = projectList;
            this.runningTask = runningTask;
            this.selectedProject = selectedProject;
        }

        public List<Project> getProjectList() {
            return projectList;
        }

        public Task getRunningTask() {
            return runningTask;
        }

        public Project getSelectedProject() {
            return selectedProject;
        }
    }
}
