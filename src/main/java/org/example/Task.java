package org.example;

import java.time.LocalDate;

public class Task {
    private int projectId;
    private String id;
    private String name;
    private String description;
    private LocalDate createDate;
    private String status;
    private int timeSpent; // Time spent in seconds
    private boolean running;

    // Constructor
    public Task(int projectId, String id, String name, String description, String createDate, String status, int timeSpent) {
        this.projectId = projectId;
        this.id = id;
        this.name = name;
        this.description = description;
        this.createDate = LocalDate.parse(createDate); // Assumes date is in ISO format (YYYY-MM-DD)
        this.status = status;
        this.timeSpent = timeSpent;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void toggleRunning() {
        this.running = !this.running;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTotalTime() { // This was previously named getTimeSpent
        int hours = timeSpent / 3600;
        int minutes = (timeSpent % 3600) / 60;
        int seconds = timeSpent % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public String getTimeSpent() { // This was previously named getTimeSpent
        int hours = timeSpent / 3600;
        int minutes = (timeSpent % 3600) / 60;
        int seconds = timeSpent % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void incrementTimeSpent() {
        timeSpent++;
    }


    public String convertSecondsStringToTimeSpent(String secondsString) {
        try {
            // Convert the input string to an integer
            int seconds = Integer.parseInt(secondsString);

            // Convert seconds to hours, minutes, and remaining seconds
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int remainingSeconds = seconds % 60;

            // Return the formatted string
            return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
        } catch (NumberFormatException e) {
            // Handle the case where the input string is not a valid integer
            System.err.println("Invalid seconds string format: " + secondsString);
            e.printStackTrace();
            return "00:00:00"; // Return default value in case of error
        }
    }



    private int parseTimeSpent(String timeSpent) {
        if (timeSpent == null || timeSpent.isEmpty()) {
            return 0; // Default to 0 if timeSpent is null or empty
        }

        String[] parts = timeSpent.split(":");

        // Default values
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        try {
            if (parts.length > 0) hours = Integer.parseInt(parts[0].trim());
            if (parts.length > 1) minutes = Integer.parseInt(parts[1].trim());
            if (parts.length > 2) seconds = Integer.parseInt(parts[2].trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid timeSpent format, using default value: " + timeSpent);
            e.printStackTrace();
        }

        return (hours * 3600) + (minutes * 60) + seconds;
    }
}
