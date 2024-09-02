package org.example;

public class User {
    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private boolean autoStart;
    private boolean hideDesktopApp;
    private boolean systemTray;
    private boolean taskBarAndSystemTray;
    private boolean screenShotNotification;
    private int timeTrackingReminderInterval;
    private String timeZone;
    private int hoursWorkedDaily;
    private int taskId;
    private String status;
    private int timeStamp;
    private boolean isRunning;
    private int timeSpent;

    public User(int userId, String firstName, String lastName, String email, String role,
                boolean autoStart, boolean hideDesktopApp, boolean systemTray,
                boolean taskBarAndSystemTray, boolean screenShotNotification, int timeTrackingReminderInterval,
                String timeZone,  int hoursWorkedDaily, int taskId, String status,
                int timeStamp, boolean isRunning, int timeSpent) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.autoStart = autoStart;
        this.hideDesktopApp = hideDesktopApp;
        this.systemTray = systemTray;
        this.taskBarAndSystemTray = taskBarAndSystemTray;
        this.screenShotNotification = screenShotNotification;
        this.timeTrackingReminderInterval = timeTrackingReminderInterval;
        this.timeZone = timeZone;
        this.hoursWorkedDaily = hoursWorkedDaily;
        this.taskId = taskId;
        this.status = status;
        this.timeStamp = timeStamp;
        this.isRunning = isRunning;
        this.timeSpent = timeSpent;
    }

    // Getters and setters for each property
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public boolean isHideDesktopApp() {
        return hideDesktopApp;
    }

    public void setHideDesktopApp(boolean hideDesktopApp) {
        this.hideDesktopApp = hideDesktopApp;
    }

    public boolean isSystemTray() {
        return systemTray;
    }

    public void setSystemTray(boolean systemTray) {
        this.systemTray = systemTray;
    }

    public boolean isTaskBarAndSystemTray() {
        return taskBarAndSystemTray;
    }

    public void setTaskBarAndSystemTray(boolean taskBarAndSystemTray) {
        this.taskBarAndSystemTray = taskBarAndSystemTray;
    }

    public boolean isScreenShotNotification() {
        return screenShotNotification;
    }

    public void setScreenShotNotification(boolean screenShotNotification) {
        this.screenShotNotification = screenShotNotification;
    }

    public int getTimeTrackingReminderInterval() {
        return timeTrackingReminderInterval;
    }

    public void setTimeTrackingReminderInterval(int timeTrackingReminderInterval) {
        this.timeTrackingReminderInterval = timeTrackingReminderInterval;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }



    public int getHoursWorkedDaily() {
        return hoursWorkedDaily;
    }

    public void setHoursWorkedDaily(int hoursWorkedDaily) {
        this.hoursWorkedDaily = hoursWorkedDaily;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public int getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(int timeSpent) {
        this.timeSpent = timeSpent;
    }
}
