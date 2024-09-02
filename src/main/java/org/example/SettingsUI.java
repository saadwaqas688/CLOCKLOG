package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class SettingsUI extends JFrame {

    public static final Color SECONDARY_COLOR = new Color(0, 36, 51);
    private JCheckBox autoStartCheckBox;
    private JCheckBox hideDesktopCheckBox;
    private JCheckBox screenshotCheckBox;
    private JCheckBox timeTrackingCheckBox;
    private JComboBox<String> showInComboBox;
    private JSpinner reminderSpinner;
    private JComboBox<String> timeZoneComboBox;
//    private Map<String, JRadioButton> languageOptions;
    private JButton saveButton;
    private JPanel mainPanel;
    private int userId;
    private Runnable onSaveCallback;
    private User user;


    public SettingsUI(User user,Runnable onSaveCallback) {
        this.user=user;
        this.onSaveCallback = onSaveCallback;

        this.userId=user.getUserId();
        setTitle("Settings");
        setSize(700, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main panel with dark background
        mainPanel = new JPanel();
        mainPanel.setBackground(SECONDARY_COLOR);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        // Settings label
        JLabel settingsLabel = new JLabel("Settings");
        configureLabel(settingsLabel, 24, Font.BOLD);
        settingsLabel.setAlignmentX(Component.LEFT_ALIGNMENT); // Align to the left

        // Options panel
        JPanel optionsPanel = new JPanel();
        optionsPanel.setBackground(SECONDARY_COLOR);
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));

        // Auto start option
        autoStartCheckBox = new JCheckBox("Auto start clocklog app on startup");
        configureCheckBox(autoStartCheckBox);
        optionsPanel.add(createLabeledComponent(autoStartCheckBox));

        // Hide desktop app option
        hideDesktopCheckBox = new JCheckBox("Hide Desktop app");
        configureCheckBox(hideDesktopCheckBox);
        optionsPanel.add(createLabeledComponent(hideDesktopCheckBox));

        // Show in dropdown
        showInComboBox = new JComboBox<>(new String[]{"Select", "Option 1", "Option 2"});
        optionsPanel.add(createDropdown("Show In", showInComboBox));

        // Screenshot notification option
        screenshotCheckBox = new JCheckBox("Screenshot Notification");
        configureCheckBox(screenshotCheckBox);
        optionsPanel.add(createLabeledComponent(screenshotCheckBox));

        // Time tracking reminder option
        timeTrackingCheckBox = new JCheckBox("Time Tracking Reminder");
        configureCheckBox(timeTrackingCheckBox);
        optionsPanel.add(createLabeledComponent(timeTrackingCheckBox));

        // Reminder time after inactivity
        JPanel reminderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reminderPanel.setBackground(SECONDARY_COLOR);
        JLabel reminderLabel = new JLabel("Remind me to track time after");
        configureLabel(reminderLabel, 14, Font.PLAIN);
        reminderPanel.add(reminderLabel);

        SpinnerModel spinnerModel = new SpinnerNumberModel(5, 0, 60, 1); // Default to 5 minutes
        reminderSpinner = new JSpinner(spinnerModel);
        reminderSpinner.setPreferredSize(new Dimension(50, 20));
        reminderPanel.add(reminderSpinner);

        JLabel minsLabel = new JLabel("mins of Inactivity.");
        configureLabel(minsLabel, 14, Font.PLAIN);
        reminderPanel.add(minsLabel);
        optionsPanel.add(reminderPanel);

        // Time zone dropdown
        timeZoneComboBox = new JComboBox<>(new String[]{"Select", "(UTC+05:00) Islamabad, Karachi"});
        optionsPanel.add(createDropdown("Time Zone", timeZoneComboBox));

        // Language options
//        JPanel languagePanel = new JPanel();
//        languagePanel.setBackground(SECONDARY_COLOR);
//        languagePanel.setLayout(new GridLayout(3, 3, 10, 10));
//        languagePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Language",
//                0, 0, new Font("SansSerif", Font.BOLD, 14), Color.WHITE));

        // Create button group to allow only one selection
//        ButtonGroup languageGroup = new ButtonGroup();
//        languageOptions = new HashMap<>();
//
//        addLanguageOption(languagePanel, languageGroup, "English");
//        addLanguageOption(languagePanel, languageGroup, "Espanol");
//        addLanguageOption(languagePanel, languageGroup, "Portugese");
//        addLanguageOption(languagePanel, languageGroup, "French");
//        addLanguageOption(languagePanel, languageGroup, "Hindi");
//        addLanguageOption(languagePanel, languageGroup, "Arabic");
//        addLanguageOption(languagePanel, languageGroup, "Mandarin");
//        addLanguageOption(languagePanel, languageGroup, "Russian");
//        addLanguageOption(languagePanel, languageGroup, "Indonesian");
//
//        optionsPanel.add(languagePanel);

        mainPanel.add(settingsLabel, BorderLayout.NORTH);
        mainPanel.add(optionsPanel, BorderLayout.CENTER);

        // Save button
        saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(80, 30));
        saveButton.setBackground(new Color(255, 87, 51));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveButton.setFocusPainted(false);
        saveButton.setAlignmentX(Component.LEFT_ALIGNMENT); // Align to the left
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSettings();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(SECONDARY_COLOR);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // Left align the save button
        buttonPanel.add(saveButton);

        add(mainPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Set default values for settings from User object
        setDefaultValues(user);

//        setVisible(true);
    }

    private void configureCheckBox(JCheckBox checkBox) {
        checkBox.setForeground(Color.WHITE);
        checkBox.setBackground(SECONDARY_COLOR);
        checkBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        checkBox.setFocusPainted(false);
    }

    private JPanel createDropdown(String label, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(SECONDARY_COLOR);

        JLabel dropdownLabel = new JLabel(label);
        configureLabel(dropdownLabel, 14, Font.PLAIN);
        panel.add(dropdownLabel);

        comboBox.setPreferredSize(new Dimension(200, 25));
        panel.add(comboBox);

        return panel;
    }

//    private void addLanguageOption(JPanel panel, ButtonGroup group, String language) {
//        JRadioButton radioButton = new JRadioButton(language);
//        radioButton.setForeground(Color.WHITE);
//        radioButton.setBackground(SECONDARY_COLOR);
//        radioButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
//        radioButton.setPreferredSize(new Dimension(120, 30)); // Increase size of radio buttons
//        radioButton.setFocusPainted(false);
//        group.add(radioButton);
//        panel.add(radioButton);
////        languageOptions.put(language, radioButton);
//    }

    private void configureLabel(JLabel label, int fontSize, int style) {
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", style, fontSize));
    }

    private JPanel createLabeledComponent(JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(SECONDARY_COLOR);
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(component);
        return panel;
    }

    private void setDefaultValues(User user) {
        autoStartCheckBox.setSelected(user.isAutoStart());
        hideDesktopCheckBox.setSelected(user.isHideDesktopApp());
        screenshotCheckBox.setSelected(user.isScreenShotNotification());
        timeTrackingCheckBox.setSelected(user.isSystemTray()); // Assuming this is the correct mapping
        showInComboBox.setSelectedIndex(1); // Assuming this maps to the "Show In" dropdown
        reminderSpinner.setValue(user.getTimeTrackingReminderInterval());
        timeZoneComboBox.setSelectedItem(user.getTimeZone());
//        languageOptions.get(user.getLanguage()).setSelected(true);
        printUserSettings("Original Values", user);


    }

    private void printUserSettings(String title, User user) {
        System.out.println(title);
        System.out.println("Auto Start: " + user.isAutoStart());
        System.out.println("Hide Desktop App: " + user.isHideDesktopApp());
        System.out.println("Screenshot Notification: " + user.isScreenShotNotification());
        System.out.println("Time Tracking Reminder: " + user.isSystemTray()); // Assuming this is the correct mapping
        System.out.println("Reminder Interval: " + user.getTimeTrackingReminderInterval() + " mins");
        System.out.println("Time Zone: " + user.getTimeZone());}

    private void saveSettings() {
        // Retrieve current values
        boolean autoStart = autoStartCheckBox.isSelected();
        boolean hideDesktop = hideDesktopCheckBox.isSelected();
        boolean systemTray = true;  // Default value or add checkbox if applicable
        boolean taskBarAndSystemTray = true;  // Default value or add checkbox if applicable
        boolean screenshotNotification = screenshotCheckBox.isSelected();
        int reminderInterval = (Integer) reminderSpinner.getValue();
        String timeZone = (String) timeZoneComboBox.getSelectedItem();
//        String selectedLanguage = languageOptions.entrySet().stream()
//                .filter(entry -> entry.getValue().isSelected())
//                .map(Map.Entry::getKey)
//                .findFirst()
//                .orElse("");

        // Assuming user ID is available in the context

        // Save the settings using SettingsUtils
//        SettingsUtils.saveUserSettings(userId, autoStart, hideDesktop, systemTray, taskBarAndSystemTray,
//                screenshotNotification, reminderInterval, timeZone, selectedLanguage);

        SettingsUtils.saveUserSettings(userId, autoStart, hideDesktop, systemTray, taskBarAndSystemTray,
                screenshotNotification, reminderInterval, timeZone);

        // Show a success message
        JOptionPane.showMessageDialog(this, "Settings saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
       user.setScreenShotNotification(screenshotNotification);
       user.setTimeTrackingReminderInterval(reminderInterval);
        onSaveCallback.run();

        // Print saved values
        System.out.println("Saved Values");
        System.out.println("Auto Start: " + autoStart);
        System.out.println("Hide Desktop App: " + hideDesktop);
        System.out.println("Screenshot Notification: " + screenshotNotification);
        System.out.println("Time Tracking Reminder: " + taskBarAndSystemTray);
        System.out.println("Reminder Interval: " + reminderInterval + " mins");
        System.out.println("Time Zone: " + timeZone);
//        System.out.println("Language: " + selectedLanguage);
    }

    // Method to get the main panel
    public JPanel getMainPanel() {
        return mainPanel;
    }


//    public static void main(String[] args) {
//        // Create dummy User object for testing
//        User testUser = new User(1, "Jhon", "Done", "jhondoe@example.com", "employee", true, true, true,langua);
//        new SettingsUI(testUser);
//    }
}
