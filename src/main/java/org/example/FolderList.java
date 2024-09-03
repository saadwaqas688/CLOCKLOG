package org.example;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;


import java.awt.Toolkit;
import java.util.stream.Collectors;


public class FolderList extends JFrame {
    private JPanel folderPanel;
    private JPanel taskPanelR;

    private JPanel currentTaskPanel;
    private JScrollPane folderScrollPane;
    private JScrollPane taskScrollPane;
    private JButton addButton;
    private List<Project> projects;
    private int selectedProjectId;
    private String selectedTaskId;
    private long lastDatabaseUpdate = 0; // Field to track the last database update time
    private long lastDatabaseUpdateForGlobalTimer = 0; // Field to track the last database update time

    private static final long UPDATE_INTERVAL_MS = 10000;
    private static final String DB_URL = DatabaseConfig.getDatabaseUrl();


    private Timer timer;
    private Task currentTask;
    private JLabel startIconGlobalTimer;
    private JLabel stopIconGlobalTimer;

    private JLabel currentTaskLabel;
    private JLabel currentTaskDetailsLabel;
    private JButton startButton;
    private JButton stopButton;

    private long elapsedTimeBeforeStop = 0;

    /* Base colors */
    public static final Color SECONDARY_COLOR = new Color(0, 36, 51); // color of componentes
    public static final Color PRIMARY_COLOR = new Color(3, 50, 70); // color of main window
    public static final Color PRIMARY_TEXT_COLOR = new Color(255, 255, 255);
    public static final Color SECONDARY_TEXT_COLOR = new Color(160, 172, 187);


    /* Status colors */
    public static final Color SUCCESS_COLOR = new Color(17, 235, 130);//Completed
    public static final Color INFO_COLOR = new Color(122, 142, 248);//Inprogress
    public static final Color WARNING_COLOR = new Color(244, 162, 97);//Review
    public static final Color ERROR_COLOR = new Color(251, 93, 93);//Todo
    public static final Color ACCENT_COLOR = new Color(231, 111, 81);
    private static final Color DARK_ORANGE = new Color(3,50,90);
    //

    private static final Color LIGHT_BLUE = new Color(173, 216, 230);
    private static final Color DARK_BLUE = new Color(153, 204, 255);

    //
    private static final Color GREEN = new Color(0, 255, 0);
    private static final Color TIMER_COLOR = new Color(0, 128, 0);
    private static final Color ADD_PROJECT_COLOR = new Color(0, 0, 0);
    private static final Color FOLDER_COLOR = new Color(255, 204, 204);
    private static final Color SEARCH_COLOR = new Color(204, 255, 204);
    private static final Color TASK_PANEL_COLOR = new Color(204, 204, 255);
    private static final Color ADD_PROJECT_PANEL_COLOR = new Color(204, 255, 204);
    private static final Color MAIN_WINDOW_COLOR = new Color(0, 36, 51); // Red color for the main window
    private int currentTaskRowIndex = -1; // Default to -1 (no task selected)
    private JTable taskTable;

    private static final int MARGIN_SIZE = 5;
    private  long IDLE_TIME_TRACKING = 3000;

    private static final long TIME_TRACKING_INTERVAL = 180;


    //

    private Timer idleTimer;


    private JLabel globalTimerLabel;
    private JButton startButtonGlobalTimer;
    private JButton stopButtonGlobalTimer;

    private Timer globalTimer;
    private long globalStartTime;
    private boolean isGlobalTimerRunning;
    private GlobalEventListener listener;
    private boolean isDefaultSelectedTaskRunning;
    private User user;
    private JPanel settingsPanel;  // Panel to hold the Settings UI
    private JPanel centerPanel;  // Panel to hold the Settings UI
    private JPanel timerPanel;  // Panel to hold the Settings UI
    private JPanel searchProjectPanel;  // Panel to hold the Settings UI
    private JPanel searchPanel;  // Panel to hold the Settings UI

    private final StatusWindow statusWindow = new StatusWindow(this);




    FolderList(User user) {

        this.user = user;

this.IDLE_TIME_TRACKING=user.getTimeTrackingReminderInterval()*60000L;


        startGlobalEventListener();
        setTitle("Custom Window");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel mainPanel = new JPanel(new BorderLayout(MARGIN_SIZE, MARGIN_SIZE));
        mainPanel.setBackground(PRIMARY_COLOR);

        addSidePanel(mainPanel);

        new UrlReceiverServer();


        centerPanel = createCenterPanel();


        centerPanel.add(createTimerPanel());

        centerPanel.add(Box.createVerticalStrut(MARGIN_SIZE));


        // Create and add Settings UI to the center panel
        settingsPanel = createSettingsPanel();
        centerPanel.add(settingsPanel);
        centerPanel.add(Box.createVerticalStrut(MARGIN_SIZE));

        // Add Project Panel

        centerPanel.add(createSearchProjectPanel());

        centerPanel.add(Box.createVerticalStrut(MARGIN_SIZE));

        // Folder Panel

        centerPanel.add(createFolderPanel());

        centerPanel.add(Box.createVerticalStrut(MARGIN_SIZE)); // Add vertical gap


        centerPanel.add(createSearchPanel());



        ProjectUtils projectUtils = new ProjectUtils();
        int userId = user.getUserId(); // Replace with the actual user ID

        // Fetch projects and additional data
        ProjectUtils.FetchProjectsResult result = projectUtils.fetchProjects(userId);

        // Access the results
        projects = result.getProjectList();
        currentTask = result.getRunningTask();
        Project selectedProject = result.getSelectedProject();

        if (selectedProject == null && !projects.isEmpty()) {
            selectedProject = projects.get(0);
        }

        selectedProjectId = selectedProject != null ? selectedProject.getId() : null;

        // Process the fetched projects
        for (Project project : projects) {
            System.out.println("Project: " + project.getName());
            for (Task task : project.getTasks()) {
                System.out.println(" - Task: " + task.getName());
            }
        }
        elapsedTimeBeforeStop = user.getHoursWorkedDaily() * 1000L;

        startGlobalTimer();





        timer = new Timer();



        populateProjects(projects);
        if (selectedProjectId != -1 ) {
            showTasks(getTasksByProjectId(selectedProjectId));
        }

        // Task Panel

        centerPanel.add(createTaskPanel());
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        setVisible(true);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                onWindowResize();
            }
        });

        if (currentTask != null) {
            startTimer(currentTask);
            statusWindow.setVisible(true);

        }
    }
    public User getUser() {
        return user;
    }

    public void callBackForChangingInSettings() {
        System.out.println("[DEBUG] Folder list refreshed."+user.isScreenShotNotification());

        listener.setIsScreenShotNotification(user.isScreenShotNotification());
        IDLE_TIME_TRACKING=user.getTimeTrackingReminderInterval()*60000L;
        // Refresh logic here...
    }


    private static class StatusWindow extends JDialog {
        private final JLabel statusLabel = new JLabel("No task is running");
        private final JButton startButton = new JButton();
        private final JButton stopButton = new JButton();
        private final FolderList outerClassInstance;  // Reference to outer class
        // Variables to store the initial mouse position
        private Point initialClick;


        StatusWindow(FolderList outerClassInstance) {
            this.outerClassInstance = outerClassInstance;  // Initialize reference

            setTitle("Task Status");

            // Convert inches to pixels
            int widthInPixels = (int) (4.2 * 96);  // 8 inches to provide more width
            int heightInPixels = (int) (0.3 * 96);  // 1 inch

            setSize(widthInPixels, heightInPixels);
            setLayout(new FlowLayout(FlowLayout.LEFT)); // Use FlowLayout to arrange buttons and label

            // Set background color to black and text color to white
            getContentPane().setBackground(SECONDARY_COLOR);
            statusLabel.setForeground(PRIMARY_TEXT_COLOR);

            // Hide the title bar
            setUndecorated(true);

            // Add a custom close button
            JButton closeButton = new JButton("\u2716"); // Unicode for a cross icon
            closeButton.setForeground(ERROR_COLOR);
            closeButton.setBackground(SECONDARY_COLOR);
            closeButton.setBorder(BorderFactory.createEmptyBorder());
            closeButton.setFocusPainted(false); // Remove the focus border


            // Add some margin to the close button
            JPanel closeButtonPanel = new JPanel(new BorderLayout());
            closeButtonPanel.setBackground(SECONDARY_COLOR);
            closeButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // 10 pixels right margin
            closeButtonPanel.add(closeButton, BorderLayout.EAST);

            // Add an action listener to the close button
            closeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose(); // Close the window
                }
            });

            // Add a custom drag button
            JButton dragButton = new JButton("\u2630"); // Unicode for a drag icon (like ☰)
            dragButton.setForeground(Color.WHITE);
            dragButton.setBackground(SECONDARY_COLOR);
            dragButton.setBorder(BorderFactory.createEmptyBorder());
            dragButton.setFocusPainted(false);

            // Add a mouse listener to enable dragging the window
            dragButton.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    initialClick = e.getPoint();
                    getComponentAt(initialClick); // Remember the initial click location
                }
            });

            dragButton.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    // Get the location of the window
                    int thisX = getLocation().x;
                    int thisY = getLocation().y;

                    // Determine how much the mouse moved since the initial click
                    int xMoved = e.getX() - initialClick.x;
                    int yMoved = e.getY() - initialClick.y;

                    // Move window to this position
                    int X = thisX + xMoved;
                    int Y = thisY + yMoved;
                    setLocation(X, Y);
                }
            });

            // Add components to the window
//            add(dragButton);
//
//            add(closeButton);
//            add(statusLabel);

            // Center the window on the screen
            setLocationRelativeTo(null);

//            this.outerClassInstance = outerClassInstance;  // Initialize reference
//
//            setTitle("Task Status");
//            // Convert inches to pixels
//            int widthInPixels = (int) (8 * 96);  // 4 inches to provide more width
//            int heightInPixels = (int) (1 * 96);  // 0.5 inches
//
//            setSize(widthInPixels, heightInPixels);
//            setLayout(new FlowLayout(FlowLayout.LEFT)); // Use FlowLayout to arrange buttons and label
//
//            // Set background color to black and text color to white
//            getContentPane().setBackground(Color.BLACK);
//            statusLabel.setForeground(Color.WHITE);

            // Load icons (adjust paths and sizes as needed)
            ImageIcon startIcon = loadImageIcon("icons/small-start-icon.png", 20, 20);
            ImageIcon stopIcon = loadImageIcon("icons/small-pause-con.png", 20, 20);

            if (startIcon != null) {
                startButton.setIcon(startIcon);
            }
            if (stopIcon != null) {
                stopButton.setIcon(stopIcon);
            }

            // Remove button text and set preferred size
            startButton.setText(null);
            stopButton.setText(null);
            startButton.setPreferredSize(new Dimension(20, 20));
            stopButton.setPreferredSize(new Dimension(20, 20));
            startButton.setBorderPainted(false);  // Remove the button border
            stopButton.setBorderPainted(false);   // Remove the button border
            startButton.setOpaque(true);
            startButton.setContentAreaFilled(true);
            stopButton.setOpaque(true);
            stopButton.setContentAreaFilled(true);

            // Set button background color
            startButton.setBackground(SECONDARY_COLOR);
            stopButton.setBackground(SECONDARY_COLOR);

            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            startButton.setVisible(true);
            stopButton.setVisible(false);

//            if(outerClassInstance.currentTask!=null){
//                startButton.setEnabled(!outerClassInstance.currentTask.isRunning());
//                stopButton.setEnabled(outerClassInstance.currentTask.isRunning());
//                startButton.setVisible(!outerClassInstance.currentTask.isRunning());
//                stopButton.setVisible(outerClassInstance.currentTask.isRunning());
//            }


            // Initial state
//            stopButton.setEnabled(false);

            // Action listeners
//            startButton.addActionListener(e -> {
//                // Logic to start the task
//                startButton.setEnabled(false);
//                stopButton.setEnabled(true);
//                updateStatus("Task is running"); // Update the status label
//            });
//
//            stopButton.addActionListener(e -> {
//                // Logic to stop the task
//                stopButton.setEnabled(false);
//                startButton.setEnabled(true);
//                updateStatus("Task is stopped"); // Update the status label
//            });
            startButton.addActionListener(e -> {
                if (outerClassInstance.currentTask != null) {
                    outerClassInstance.startTimer(outerClassInstance.currentTask);
                    outerClassInstance.currentTask.setRunning(true);
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                    startButton.setVisible(false);
                    stopButton.setVisible(true);
//                    updateStatus("stop");
                }
            });

            stopButton.addActionListener(e -> {
                if (outerClassInstance.currentTask != null) {
                    outerClassInstance.stopTimer();
                    outerClassInstance.currentTask.setRunning(false);
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    startButton.setVisible(true);
                    stopButton.setVisible(false);
//                    updateStatus("start");
                }
            });


            add(dragButton);
            add(startButton);
            add(stopButton);
            add(statusLabel);
            add(closeButtonPanel, BorderLayout.EAST);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null); // Center on the screen
            setAlwaysOnTop(true); // Keep the status window on top
        }

        void updateStatus(String status,Task task) {
            if(task!=null){
                startButton.setEnabled(!task.isRunning());
                stopButton.setEnabled(task.isRunning());
                startButton.setVisible(!task.isRunning());
                stopButton.setVisible(task.isRunning());
            }

            statusLabel.setText(status);
            statusLabel.revalidate(); // Ensure the layout is updated
            statusLabel.repaint();    // Repaint the component to reflect changes
        }
    }


    private void updateStatusWindow() {
        String status = currentTask != null ? "  "+currentTask.getName() +"-"+ "Time Spent: " + currentTask.getTimeSpent() + " s"+"     " :
                "No task is running"+"     " ;
        statusWindow.updateStatus(status,currentTask);
    }
//

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical stacking
        centerPanel.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));
        centerPanel.setBackground(PRIMARY_COLOR);
        return centerPanel;
    }









    private JPanel createTimerPanel() {
        timerPanel = new JPanel();
        int fixedHeight = 60; // Set the fixed height
        timerPanel.setPreferredSize(new Dimension(getWidth(), fixedHeight));
        timerPanel.setMinimumSize(new Dimension(0, fixedHeight)); // Prevent shrinking below this height
        timerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fixedHeight)); //
        timerPanel.setBackground(SECONDARY_COLOR);
        timerPanel.setLayout(new BorderLayout());
        timerPanel.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));

        // Current Task Labels
        currentTaskLabel = new JLabel("Project Name");
        currentTaskLabel.setHorizontalAlignment(SwingConstants.LEFT);
        currentTaskLabel.setFont(new Font("Arial", Font.PLAIN, 15)); // Adjust font name and size as needed
        Font boldFont1 = currentTaskLabel.getFont().deriveFont(Font.BOLD);
        currentTaskLabel.setFont(boldFont1);



        currentTaskDetailsLabel = new JLabel("No task selected");
        currentTaskLabel.setForeground(PRIMARY_TEXT_COLOR); // Set text color
        currentTaskDetailsLabel.setForeground(PRIMARY_TEXT_COLOR); // Set text color

        // Add spacing between project name and task details
        currentTaskDetailsLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0)); // Adds top margin of 5 pixels

        // Panel for Task Information
        JPanel taskInfoPanel = new JPanel();
        taskInfoPanel.setLayout(new BoxLayout(taskInfoPanel, BoxLayout.Y_AXIS)); // Stack components vertically
        taskInfoPanel.setBackground(SECONDARY_COLOR); // Match timer panel color
        taskInfoPanel.setOpaque(false); // Make transparent
        taskInfoPanel.add(currentTaskLabel);
        taskInfoPanel.add(currentTaskDetailsLabel);
        taskInfoPanel.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)); // Add padding

        // Timer Label
        globalTimerLabel = new JLabel("00:00:00", SwingConstants.LEFT);
        globalTimerLabel.setForeground(PRIMARY_TEXT_COLOR); // Set text color
        globalTimerLabel.setFont(new Font("Arial", Font.PLAIN, 24)); // Adjust font name and size as needed
        Font boldFont = globalTimerLabel.getFont().deriveFont(Font.BOLD);
        globalTimerLabel.setFont(boldFont);
        globalTimerLabel.setOpaque(false); // Make label transparent

        // Load icons for start and stop
        ImageIcon startIcon = loadImageIcon("icons/small-start-icon.png", 30, 30); // Adjust path and size as needed
        ImageIcon stopIcon = loadImageIcon("icons/small-pause-con.png", 30, 30);   // Adjust path and size as needed

        // Start Icon
        startIconGlobalTimer = new JLabel();
        if (startIcon != null) {
            startIconGlobalTimer.setIcon(startIcon); // Set the icon
        }
        startIconGlobalTimer.setPreferredSize(new Dimension(30, 30)); // Set preferred size
        startIconGlobalTimer.setOpaque(false); // Make label transparent
        startIconGlobalTimer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startGlobalTimer();
            }
        });

        // Stop Icon
        stopIconGlobalTimer = new JLabel();
        if (stopIcon != null) {
            stopIconGlobalTimer.setIcon(stopIcon); // Set the icon
        }
        stopIconGlobalTimer.setPreferredSize(new Dimension(30, 30)); // Set preferred size
        stopIconGlobalTimer.setOpaque(false); // Make label transparent
        stopIconGlobalTimer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                stopGlobalTimer();
            }
        });

        // Initially, make the stop icon invisible
        stopIconGlobalTimer.setVisible(true);
        stopIconGlobalTimer.setVisible(false);

        // Panel for Timer and Icons
        JPanel timerAndIconsPanel = new JPanel();
        timerAndIconsPanel.setLayout(new GridBagLayout());
        timerAndIconsPanel.setBackground(SECONDARY_COLOR); // Set background color
        timerAndIconsPanel.setOpaque(false); // Make transparent
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5); // Spacing between components
        gbc.anchor = GridBagConstraints.WEST;

        // Add Timer Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        timerAndIconsPanel.add(globalTimerLabel, gbc);

        // Add Start Icon
        gbc.gridx = 1;
        timerAndIconsPanel.add(startIconGlobalTimer, gbc);

        // Add Stop Icon
        gbc.gridx = 2;
        timerAndIconsPanel.add(stopIconGlobalTimer, gbc);

        // Username Label
        JLabel userAvatar = FolderListUtils.createUserAvatar("images/nabeel.jpg", "Username");


        // Panel to align Timer and Icons
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(timerAndIconsPanel, BorderLayout.CENTER);
        rightPanel.add(userAvatar, BorderLayout.EAST);
        rightPanel.setBackground(SECONDARY_COLOR); // Set background color
        rightPanel.setOpaque(false); // Make transparent

        // Add Components to Timer Panel
        timerPanel.add(taskInfoPanel, BorderLayout.WEST);
        timerPanel.add(rightPanel, BorderLayout.EAST);

        return timerPanel;
    }
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    // Method to create the settings panel
    private JPanel createSettingsPanel() {
        SettingsUI settingsUI = new SettingsUI(user,this::callBackForChangingInSettings
        );
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PRIMARY_COLOR);
        panel.add(settingsUI.getMainPanel(), BorderLayout.CENTER);
        panel.setVisible(false); // Initially, the settings panel is hidden
        return panel;
    }

    // Method to show the dashboard
    private void showDashboard() {


        settingsPanel.setVisible(false);  // Panel to hold the Settings UI
//        centerPanel.setVisible(true);  // Panel to hold the Settings UI
//        timerPanel.setVisible(true);  // Panel to hold the Settings UI
        searchProjectPanel.setVisible(true);  // Panel to hold the Settings UI
        folderScrollPane.setVisible(true);
        searchPanel.setVisible(true);  // Panel to hold the Settings UI
        taskPanelR.setVisible(true);
    }

    // Method to show the settings
    private void showSettings() {
        settingsPanel.setVisible(true);  // Panel to hold the Settings UI
//        centerPanel.setVisible(true);  // Panel to hold the Settings UI
//        timerPanel.setVisible(t);  // Panel to hold the Settings UI
        searchProjectPanel.setVisible(false);  // Panel to hold the Settings UI
        folderScrollPane.setVisible(false);
        searchPanel.setVisible(false);  // Panel to hold the Settings UI
        taskPanelR.setVisible(false);

    }
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //search project panel
    private JPanel createSearchProjectPanel() {
        searchProjectPanel = new JPanel();
        int fixedHeight = 60; // Set the fixed height
        searchProjectPanel.setPreferredSize(new Dimension(getWidth(), fixedHeight));
        searchProjectPanel.setMinimumSize(new Dimension(0, fixedHeight)); // Prevent shrinking below this height
        searchProjectPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fixedHeight)); //
        searchProjectPanel.setBackground(SECONDARY_COLOR);
//        searchProjectPanel.setLayout(new BorderLayout());
//        searchProjectPanel.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));
        searchProjectPanel.setLayout(new BoxLayout(searchProjectPanel, BoxLayout.X_AXIS));
        searchProjectPanel.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));
        ImageIcon searchIcon = loadImageIcon("icons/search-icon.png", 20, 20);

        SearchTask searchField = new SearchTask("Search Project", SECONDARY_TEXT_COLOR, PRIMARY_TEXT_COLOR,searchIcon);
        searchField.setBackground(PRIMARY_COLOR);
//        searchField.setPreferredSize(new Dimension(180, 35)); // Set preferred size for text field
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Add padding

        searchField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!searchField.hasFocus()) {
                    searchField.setFocusable(true);
                    searchField.requestFocus();
                }
            }
        });

        // Add focus listener to handle losing focus
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                searchField.setFocusable(false);
            }
        });

        // Add a mouse listener to the frame to lose focus when clicking outside
        searchProjectPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchField.hasFocus() && !searchField.getBounds().contains(e.getPoint())) {
                    searchField.transferFocus(); // Moves focus to the next component
                }
            }
        });
        // Add a DocumentListener to the search field to handle text changes
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterProjects();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterProjects();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterProjects();
            }

            private void filterProjects() {
                String searchText = searchField.getText().trim().toLowerCase();
                if (searchText.isEmpty()) {
                    // If the search field is empty, show the original list of projects
                    populateProjects(projects);
                } else {
                    // Filter the projects based on the search text
                    List<Project> filteredProjects = projects.stream()
                            .filter(project -> project.getName().toLowerCase().contains(searchText))
                            .collect(Collectors.toList());

                    // Update the UI with the filtered projects
                    populateProjects(filteredProjects);
                }
            }
        });

        searchProjectPanel.add(searchField, BorderLayout.WEST);

        return searchProjectPanel;
    }



    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //

    //side panel container
    private void addSidePanel(JPanel mainPanel) {
        // Side Panel with BorderLayout and margins
        JPanel sidePanelContainer = new JPanel(new BorderLayout());
        sidePanelContainer.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)); // Top, Left, Bottom, Right
        sidePanelContainer.setBackground(PRIMARY_COLOR); // Match background to main panel

        JPanel sidePanel = new JPanel();
        sidePanel.setPreferredSize(new Dimension(144, getHeight()));
        sidePanel.setBackground(SECONDARY_COLOR);
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

        // Load icons (adjust paths and sizes as needed)
        ImageIcon icon1 = loadImageIcon("icons/hollow-clock-icon.png", 30, 30);
        ImageIcon icon2 = loadImageIcon("icons/dashboard-icon.png", 30, 30);
        ImageIcon icon3 = loadImageIcon("icons/setting-icon.png", 30, 30);
        ImageIcon icon4 = loadImageIcon("icons/clocklog-icon-with-name.png",80,80);

        // Create labels with icons
        JLabel label1 = new JLabel();
        JLabel label2 = new JLabel();
        JLabel label3 = new JLabel();
        JLabel label4 = new JLabel();


        if (icon1 != null) {
            label1.setIcon(icon1);
        }
        if (icon2 != null) {
            label2.setIcon(icon2);
        }
        if (icon3 != null) {
            label3.setIcon(icon3);
        }

        if (icon4 != null) {
            label4.setIcon(icon4);
        }

        // Align labels in the center
        label4.setAlignmentX(Component.CENTER_ALIGNMENT);
        label1.setAlignmentX(Component.CENTER_ALIGNMENT);
        label2.setAlignmentX(Component.CENTER_ALIGNMENT);
        label3.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add MouseListener to label2 (Dashboard)
        label2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showDashboard();
            }
        });

        // Add MouseListener to label3 (Settings)
        label3.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showSettings();
            }
        });



        // Add spacing and labels to the panel
        sidePanel.add(Box.createVerticalStrut(30)); // Space at the top
        sidePanel.add(label4);

        sidePanel.add(Box.createVerticalStrut(30)); // Space at the top
        sidePanel.add(label1);
        sidePanel.add(Box.createVerticalStrut(50)); // Space between icons
        sidePanel.add(label2);
        sidePanel.add(Box.createVerticalStrut(50)); // Space between icons
        sidePanel.add(label3);
        sidePanel.add(Box.createVerticalGlue()); // Pushes icons to the top and bottom

        // Add sidePanel to sidePanelContainer
        sidePanelContainer.add(sidePanel, BorderLayout.CENTER);

        // Add sidePanelContainer to mainPanel
        mainPanel.add(sidePanelContainer, BorderLayout.WEST);
    }

    // Method to create the folder panel with scroll functionality

    private JScrollPane createFolderPanel() {
        folderPanel = new JPanel();
        folderPanel.setPreferredSize(new Dimension(getWidth(), 180));
        folderPanel.setBackground(SECONDARY_COLOR);
        folderPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // Create JScrollPane for folderPanel
        folderScrollPane = new JScrollPane(folderPanel);
        folderScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        folderScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        folderScrollPane.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));

        folderScrollPane.getViewport().setOpaque(false);
        folderScrollPane.setOpaque(false);
        folderScrollPane.setBackground(new Color(0, 0, 0, 0)); // Fully transparent background

        // Customize the horizontal scrollbar
        JScrollBar horizontalScrollBar = folderScrollPane.getHorizontalScrollBar();
        horizontalScrollBar.setUnitIncrement(16); // Adjust horizontal scrollbar sensitivity
        horizontalScrollBar.setBackground(SECONDARY_COLOR);


        horizontalScrollBar.setUI(new BasicScrollBarUI() {
            private final int thumbSize = (int) (2 * 3.78); // Convert 1 mm to pixels
            private final int trackSize = (int) (2 * 3.78); // Convert 1 mm to pixels
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = PRIMARY_COLOR;
                this.trackColor = SECONDARY_COLOR;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createInvisibleButton(); // Hide the left arrow button
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createInvisibleButton(); // Hide the right arrow button
            }

            private JButton createInvisibleButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                button.setOpaque(false);
                button.setContentAreaFilled(false);
                button.setBorder(BorderFactory.createEmptyBorder());
                return button;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbSize, thumbSize, thumbSize); // Use thumbSize for corner radius
                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(trackColor);
                g2.fillRoundRect(trackBounds.x, trackBounds.y, trackBounds.width, trackSize, trackSize, trackSize); // Use trackSize for corner radius
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize(JComponent c) {
                return new Dimension(16, 8); // Set width and height here
            }
        });

        // Round corners of the JScrollPane
        folderScrollPane.setViewportBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1, true)); // True for rounded corners

        return folderScrollPane;
    }




//    private JScrollPane createFolderPanel() {
//        folderPanel = new JPanel();
//        folderPanel.setPreferredSize(new Dimension(getWidth(), 180));
//        folderPanel.setBackground(SECONDARY_COLOR);
//        folderPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
////        folderPanel.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));
//
//        // Create JScrollPane for folderPanel
//        folderScrollPane = new JScrollPane(folderPanel);
//        folderScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//        folderScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
//        folderScrollPane.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));
//
//        folderScrollPane.getViewport().setOpaque(false);
//        folderScrollPane.setOpaque(false);
//        folderScrollPane.setBackground(Color.red);
//
//        // Customize the horizontal scrollbar
//        JScrollBar horizontalScrollBar = folderScrollPane.getHorizontalScrollBar();
//        horizontalScrollBar.setUnitIncrement(16); // Adjust horizontal scrollbar sensitivity
//        horizontalScrollBar.setUI(new BasicScrollBarUI() {
//            @Override
//            protected void configureScrollBarColors() {
//                this.thumbColor = Color.GREEN; // Set thumb color
//                this.trackColor = Color.red;      // Set track color (optional)
//            }
//
//            @Override
//            protected JButton createDecreaseButton(int orientation) {
//                return createInvisibleButton(); // Hide the left arrow button
//            }
//
//            @Override
//            protected JButton createIncreaseButton(int orientation) {
//                return createInvisibleButton(); // Hide the right arrow button
//            }
//
//            private JButton createInvisibleButton() {
//                JButton button = new JButton();
//                button.setPreferredSize(new Dimension(0, 0));
//                button.setMinimumSize(new Dimension(0, 0));
//                button.setMaximumSize(new Dimension(0, 0));
//                return button;
//            }
//        });
//
//        // Round corners of the JScrollPane
//        folderScrollPane.setViewportBorder(BorderFactory.createLineBorder(SECONDARY_COLOR, 1, true)); // True for rounded corners
//
//        return folderScrollPane;
//    }


    private static ImageIcon loadImageIcon(String path, int width, int height) {
        // Load the image from the classpath
        ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource(path));
        if (icon != null) {
            Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        } else {
            System.out.println("Image not found at path: " + path);
            return null;
        }
    }

    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
//    private JPanel createSearchPanel() {
//        searchPanel = new JPanel();
//        int fixedHeight = 60; // Set the fixed height
//        searchPanel.setPreferredSize(new Dimension(getWidth(), fixedHeight));
//        searchPanel.setMinimumSize(new Dimension(0, fixedHeight)); // Prevent shrinking below this height
//        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fixedHeight)); //
//
//        searchPanel.setBackground(SECONDARY_COLOR);
//        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
//
//        searchPanel.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));
//
////        searchPanel.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));
//
//        // Use the custom TextField class with a placeholder and color settings
//        ImageIcon searchIcon = loadImageIcon("icons/search-icon.png", 20, 20);
//
//        SearchTask searchField = new SearchTask("Search Task", SECONDARY_TEXT_COLOR, PRIMARY_TEXT_COLOR,searchIcon);
//        searchField.setBackground(PRIMARY_COLOR);
//        searchField.setPreferredSize(new Dimension(180, 35)); // Set preferred size for text field
//        searchField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Add padding
//
//        ImageIcon addTaskIcon = loadImageIcon("icons/add-project-icon.png", 30, 30); // Adjust path and size as needed
//        JButton addTaskButton = new JButton();
//        if (addTaskIcon != null) {
//            addTaskButton.setIcon(addTaskIcon); // Set the icon
//        } else {
//            addTaskButton.setText("Add Task"); // Fallback text if icon not found
//        }
//        searchField.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (!searchField.hasFocus()) {
//                    searchField.setFocusable(true);
//                    searchField.requestFocus();
//                }
//            }
//        });
//
//        // Add focus listener to handle losing focus
//        searchField.addFocusListener(new FocusAdapter() {
//            @Override
//            public void focusLost(FocusEvent e) {
//                searchField.setFocusable(false);
//            }
//        });
//
//        // Add a mouse listener to the frame to lose focus when clicking outside
//        searchPanel.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                if (searchField.hasFocus() && !searchField.getBounds().contains(e.getPoint())) {
//                    searchField.transferFocus(); // Moves focus to the next component
//                }
//            }
//        });
//
//        addTaskButton.addActionListener(e -> showAddTaskDialog());
//        addTaskButton.setBackground(WARNING_COLOR);
//        addTaskButton.setPreferredSize(new Dimension(90, 20));
//
//        searchPanel.add(searchField, BorderLayout.WEST);
//        searchPanel.add(addTaskButton, BorderLayout.EAST);
//
//        // Add DocumentListener to searchField to listen for changes in text
//        searchField.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                filterTasks();
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                filterTasks();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                filterTasks();
//            }
//
//            private void filterTasks() {
//                String searchText = searchField.getText().trim().toLowerCase();
//                if (searchText.isEmpty()) {
//                    showTasks(getTasksByProjectId(selectedProjectId)); // Show tasks of the selected project
//                } else {
//                    List<Task> filteredTasks = projects.stream()
//                            .flatMap(project -> project.getTasks().stream())
//                            .filter(task -> task.getName().toLowerCase().contains(searchText))
//                            .collect(Collectors.toList());
//                    showTasks(filteredTasks);
//                }
//            }
//        });
//
//        return searchPanel;
//    }
//

    private JPanel createSearchPanel() {
        searchPanel = new JPanel();
        int fixedHeight = 60; // Set the fixed height
        searchPanel.setPreferredSize(new Dimension(getWidth(), fixedHeight));
        searchPanel.setMinimumSize(new Dimension(0, fixedHeight)); // Prevent shrinking below this height
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, fixedHeight)); //

        searchPanel.setBackground(SECONDARY_COLOR);
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS)); // Use BoxLayout to stack components horizontally
        searchPanel.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));

        // Use the custom TextField class with a placeholder and color settings
        ImageIcon searchIcon = loadImageIcon("icons/search-icon.png", 20, 20);

        SearchTask searchField = new SearchTask("Search Task", SECONDARY_TEXT_COLOR, PRIMARY_TEXT_COLOR, searchIcon);
        searchField.setBackground(PRIMARY_COLOR);
//        searchField.setPreferredSize(new Dimension(180, 35)); // Set preferred size for text field
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Add padding

        // Create status dropdown
//        String[] statuses = {"Completed", "Review", "ToDo", "InProgress"};
//        JComboBox<String> statusDropdown = new JComboBox<>(statuses);
//        statusDropdown.setPreferredSize(new Dimension(120, 35)); // Set preferred size for the dropdown
//
//        // Add action listener to handle status changes
//        statusDropdown.addActionListener(e -> {
//            String selectedStatus = (String) statusDropdown.getSelectedItem();
//            filterTasksByStatus(selectedStatus);
//        });

//        JFrame frame = new JFrame();
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setLayout(new FlowLayout());

        // Create the status options
          CustomStatusComboBox statusDropdown = new CustomStatusComboBox();


        // Set dimensions and background color
//        statusDropdown.setPreferredSize(new Dimension(180, 25));
        int fixed = 33; // Set the fixed height
        statusDropdown.setPreferredSize(new Dimension(180, fixed));
        statusDropdown.setMinimumSize(new Dimension(180, fixed)); // Prevent shrinking below this height
        statusDropdown.setMaximumSize(new Dimension(180, fixed)); //

        statusDropdown.setBackground(PRIMARY_COLOR);
        statusDropdown.setForeground(PRIMARY_TEXT_COLOR); // Optionally set text color


        // Wrap the dropdown in a panel to add left margin
        JPanel dropdownPanel = new JPanel();
        dropdownPanel.setLayout(new BoxLayout(dropdownPanel, BoxLayout.X_AXIS)); // Use BoxLayout for horizontal layout
        dropdownPanel.setOpaque(false); // Make the panel transparent

        // Create a border with left margin
        dropdownPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        dropdownPanel.add(statusDropdown);


        // Add ActionListener to handle selection changes
        statusDropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedStatus = (String) statusDropdown.getSelectedItem();
                filterTasksByStatus(selectedStatus); // Call the filter method with the selected status
            }
        });

        ImageIcon addTaskIcon = loadImageIcon("icons/add-project-icon.png", 30, 30); // Adjust path and size as needed
        JButton addTaskButton = new JButton();
        if (addTaskIcon != null) {
            addTaskButton.setIcon(addTaskIcon); // Set the icon
        } else {
            addTaskButton.setText("Add Task"); // Fallback text if icon not found
        }
        addTaskButton.setBackground(WARNING_COLOR);
//        addTaskButton.setPreferredSize(new Dimension(90, 35)); // Adjust size to match the fixed height
        addTaskButton.setPreferredSize(new Dimension(70, fixed));
        addTaskButton.setMinimumSize(new Dimension(70, fixed)); // Prevent shrinking below this height
        addTaskButton.setMaximumSize(new Dimension(70, fixed)); //
        // Wrap the button in a panel to help with layout
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false); // Make the panel transparent
        buttonPanel.add(Box.createHorizontalGlue()); // Push the button to the right
        buttonPanel.add(addTaskButton);

        searchField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!searchField.hasFocus()) {
                    searchField.setFocusable(true);
                    searchField.requestFocus();
                }
            }
        });

        // Add focus listener to handle losing focus
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                searchField.setFocusable(false);
            }
        });

        // Add a mouse listener to the panel to lose focus when clicking outside
        searchPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchField.hasFocus() && !searchField.getBounds().contains(e.getPoint())) {
                    searchField.transferFocus(); // Moves focus to the next component
                }
            }
        });

        addTaskButton.addActionListener(e -> showAddTaskDialog());

        // Add the search field, dropdown, and button panel to the searchPanel
        searchPanel.add(searchField);
        searchPanel.add(dropdownPanel); // Add dropdown right after the search field
        searchPanel.add(buttonPanel); // Add button panel to push button to the right

        // Add DocumentListener to searchField to listen for changes in text
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTasks();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTasks();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTasks();
            }

            private void filterTasks() {
                String searchText = searchField.getText().trim().toLowerCase();
                if (searchText.isEmpty()) {
                    showTasks(getTasksByProjectId(selectedProjectId)); // Show tasks of the selected project
                } else {
                    List<Task> filteredTasks = getTasksByProjectId(selectedProjectId).stream()
                            .filter(task -> task.getName().toLowerCase().contains(searchText))
                            .collect(Collectors.toList());
                    showTasks(filteredTasks);
                }
            }
        });

        return searchPanel;
    }

    // Method to filter tasks based on the selected status
    private void filterTasksByStatus(String status) {
        List<Task> filteredTasks =getTasksByProjectId(selectedProjectId).stream()
                .filter(task -> task.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
        showTasks(filteredTasks);
    }


//    private JPanel createSearchPanel() {
//        JPanel searchPanel = new JPanel();
//        searchPanel.setPreferredSize(new Dimension(getWidth(), 60));// Fixed height of 29 pixels
//        searchPanel.setBackground(SECONDARY_COLOR);
//        searchPanel.setLayout(new BorderLayout());
//        searchPanel.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));
//
//        JTextField searchField = new JTextField();
//        searchField.setBackground(PRIMARY_COLOR);
//        searchField.setPreferredSize(new Dimension(180,35));// Set preferred size for text field
//
//        ImageIcon addTaskIcon = loadImageIcon("icons/add-project-icon.png", 30, 30); // Adjust path and size as needed
//
//        JButton addTaskButton = new JButton();
//        if (addTaskIcon != null) {
//            addTaskButton.setIcon(addTaskIcon); // Set the icon
//        } else {
//            addTaskButton.setText("Add Task"); // Fallback text if icon not found
//        }
//
//
//        addTaskButton.addActionListener(e -> {
//            showAddTaskDialog();
//
//        });
//
//        addTaskButton.setBackground(WARNING_COLOR);
//        addTaskButton.setPreferredSize(new Dimension(90, 20));
//
//        searchPanel.add(searchField, BorderLayout.WEST);
//        searchPanel.add(addTaskButton, BorderLayout.EAST);
//
//        return searchPanel;
//    }
//private JPanel createSearchPanel() {
//    JPanel searchPanel = new JPanel();
//    searchPanel.setPreferredSize(new Dimension(getWidth(), 60)); // Fixed height of 60 pixels
//    searchPanel.setBackground(SECONDARY_COLOR);
//    searchPanel.setLayout(new BorderLayout());
//    searchPanel.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));
//
//    JTextField searchField = new JTextField();
//    searchField.setBackground(PRIMARY_COLOR);
//    searchField.setPreferredSize(new Dimension(180, 35)); // Set preferred size for text field
//    searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Add padding (top, left, bottom, right)
//
//    // Set placeholder text (using a custom approach for Java)
//    searchField.setForeground(PRIMARY_TEXT_COLOR);
//    searchField.setText("Search Task");
//
//    searchField.addFocusListener(new FocusAdapter() {
//        @Override
//        public void focusGained(FocusEvent e) {
//            if (searchField.getText().equals("Search Task")) {
//                searchField.setText("");
//                searchField.setForeground(PRIMARY_TEXT_COLOR);
//            }
//        }
//
//        @Override
//        public void focusLost(FocusEvent e) {
//            if (searchField.getText().isEmpty()) {
//                searchField.setForeground(PRIMARY_TEXT_COLOR);
//                searchField.setText("Search Task");
//            }
//        }
//    });
//
//    // Add a DocumentListener to the search field to handle text changes
//    searchField.getDocument().addDocumentListener(new DocumentListener() {
//        @Override
//        public void insertUpdate(DocumentEvent e) {
//            filterTasks();
//        }
//
//        @Override
//        public void removeUpdate(DocumentEvent e) {
//            filterTasks();
//        }
//
//        @Override
//        public void changedUpdate(DocumentEvent e) {
//            filterTasks();
//        }
//
//        private void filterTasks() {
//            String searchText = searchField.getText().trim().toLowerCase();
//            Project selectedProject = getSelectedProject();
//            if (selectedProject == null) {
//                return; // No project selected
//            }
//
//            List<Task> tasks = selectedProject.getTasks();
//            if (tasks == null) {
//                return; // No tasks available
//            }
//
//            if (searchText.isEmpty()) {
//                // If the search field is empty, show the original list of tasks
//                showTasks(getTasksByProjectId(selectedProject.getId()));
//            } else {
//                // Filter the tasks based on the search text
//                List<Task> filteredTasks = tasks.stream()
//                        .filter(task -> task.getName().toLowerCase().contains(searchText))
//                        .collect(Collectors.toList());
//
//                // Update the UI with the filtered tasks
//                updateTaskTable(filteredTasks);
//            }
//        }
//    });
//
//    ImageIcon addTaskIcon = loadImageIcon("icons/add-project-icon.png", 30, 30); // Adjust path and size as needed
//
//    JButton addTaskButton = new JButton();
//    if (addTaskIcon != null) {
//        addTaskButton.setIcon(addTaskIcon); // Set the icon
//    } else {
//        addTaskButton.setText("Add Task"); // Fallback text if icon not found
//    }
//
//    addTaskButton.addActionListener(e -> {
//        showAddTaskDialog();
//    });
//
//    addTaskButton.setBackground(WARNING_COLOR);
//    addTaskButton.setPreferredSize(new Dimension(90, 20));
//
//    searchPanel.add(searchField, BorderLayout.WEST);
//    searchPanel.add(addTaskButton, BorderLayout.EAST);
//
//    return searchPanel;
//}
//
//    // Helper method to get the selected project (you can implement this based on your application's logic)
//    private Project getSelectedProject() {
//        return projects.stream().filter(p -> p.getId()==selectedProjectId).findFirst().orElse(null);
//    }
//
//    // Helper method to update the task table with a filtered list of tasks
//    private void updateTaskTable(List<Task> filteredTasks) {
//        String[] columnNames = {"Task", "Description", "Create Date", "Status", "Total Time"};
//        Object[][] taskData = new Object[filteredTasks.size()][5];
//
//        for (int i = 0; i < filteredTasks.size(); i++) {
//            Task task = filteredTasks.get(i);
//            taskData[i][0] = task;  // Store the Task object itself
//            taskData[i][1] = task.getDescription();
//            taskData[i][2] = task.getCreateDate().toString();
//            taskData[i][3] = task.getStatus();
//            taskData[i][4] = task.getTotalTime();
//        }
//
//        DefaultTableModel tableModel = new DefaultTableModel(taskData, columnNames) {
//            @Override
//            public boolean isCellEditable(int row, int column) {
//                return column == 0 || column == 3; // Editable for Task and Status columns
//            }
//
//            @Override
//            public Class<?> getColumnClass(int column) {
//                if (column == 0) return Task.class; // Custom class for task column
//                return String.class;
//            }
//        };
//
//        taskTable.setModel(tableModel);
//    }

    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //

    private Project createProject(ResultSet rs) {
        try {
            int projectId = rs.getInt("projectId");
            String projectName = rs.getString("projectName");
            String projectDescription = rs.getString("projectDescription");

            System.out.println("Creating new project: " + projectId + " - " + projectName);
            return new Project(
                    projectId,
                    projectName,
                    projectDescription,
                    new ArrayList<>()
            );
        } catch (SQLException e) {
            System.err.println("Error creating project: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }




    //task panel
    private JPanel createTaskPanel() {
        taskPanelR = new JPanel();
        taskPanelR.setBackground(SECONDARY_COLOR);
        taskPanelR.setLayout(new BorderLayout());
        taskPanelR.setBorder(BorderFactory.createEmptyBorder(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));

        String[] columnNames = {"Name", "Description", "Create Date", "Status", "Total Time"};
        Object[][] data = {
                {"Task 1", "Description 1", "2024-07-23", "Open", "00:00:00"},
                {"Task 2", "Description 2", "2024-07-23", "In Progress", "00:00:00"}
        };

        taskTable = new JTable(data, columnNames);

        // Set default renderer for all cells
        TableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(Color.BLACK); // Set background color to black
                c.setForeground(Color.WHITE); // Set text color to white for contrast
                return c;
            }
        };
        taskTable.setDefaultRenderer(Object.class, renderer);

        // Set table header background color to black
        JTableHeader header = taskTable.getTableHeader();
        header.setPreferredSize(new Dimension(100, 40));

        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(SECONDARY_COLOR); // Set header background color to black
                label.setForeground(Color.WHITE); // Set header text color to white
                label.setHorizontalAlignment(SwingConstants.CENTER); // Center-align header text
                return label;
            }
        });


        taskTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(Color.BLACK); // Set cell background color to black
                label.setForeground(Color.WHITE); // Set cell text color to white
                label.setHorizontalAlignment(SwingConstants.CENTER); // Center-align cell text
                label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Add padding to cells
                return label;
            }
        });

        // Set table properties
        taskTable.setBackground(Color.BLACK); // Set background color for empty areas of the table
        taskTable.setForeground(Color.WHITE); // Set default text color
        taskTable.setGridColor(Color.GRAY); // Set grid line color
        taskTable.setSelectionBackground(Color.DARK_GRAY); // Set selection background color
        taskTable.setBorder(BorderFactory.createEmptyBorder()); // Remove cell borders

        // Remove table header border
        header.setBorder(BorderFactory.createEmptyBorder());

        // Add table to a scroll pane and remove scroll pane border
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove scroll pane border

//        frame.add(scrollPane);
//        frame.pack();
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);

        JScrollPane tableScrollPane = new JScrollPane(taskTable);

        taskPanelR.add(tableScrollPane, BorderLayout.CENTER);

        return taskPanelR;
    }












    private void startGlobalEventListener() {
        Thread eventListenerThread = new Thread(() -> {
            try {
                listener = new GlobalEventListener(user.isScreenShotNotification());
                listener.openLogFile();
                listener.startPeriodicLogging();

                try {
                    // Register the native hook
                    GlobalScreen.registerNativeHook();

                    // Add keyboard listener first
                    GlobalScreen.addNativeKeyListener(listener);
                    GlobalScreen.addNativeMouseMotionListener(listener);

                    // Add a shutdown hook to unregister the native hook and close the log file
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            GlobalScreen.unregisterNativeHook();
                            listener.closeLogFile();
                        } catch (NativeHookException | IOException e) {
                            e.printStackTrace();
                        }
                    }));

                } catch (NativeHookException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        eventListenerThread.start();
    }

    private void addUserProjectEntry(int projectId) {
        String sql = "INSERT INTO user-projects (userId, projectId, isSelected) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user.getUserId());
            pstmt.setInt(2, projectId);
            pstmt.setBoolean(3, false); // Default isSelected value
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error adding user-project entry to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void populateProjects(List<Project> projects) {
        folderPanel.removeAll();
        for (Project project : projects) {
            // Calculate task counts
            int completedCount = 0;
            int reviewCount = 0;
            int inProgressCount = 0;
            int toDoCount = 0;
            // Example data - replace with actual task fetching and counting
            for (Task task : project.getTasks()) {
                switch (task.getStatus()) {
                    case "Completed":
                        completedCount++;
                        break;
                    case "Review":
                        reviewCount++;
                        break;
                    case "InProgress":
                        inProgressCount++;
                        break;
                    case "ToDo":
                        toDoCount++;
                        break;
                }
            }

            // Create JPanel for folder content
            JPanel folderLabelPanel = new JPanel();
            folderLabelPanel.setPreferredSize(new Dimension(200, 120));
            folderLabelPanel.setOpaque(true);
            folderLabelPanel.setBackground(project.getId()==selectedProjectId ? DARK_ORANGE : PRIMARY_COLOR);
            folderLabelPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            folderLabelPanel.setLayout(new GridBagLayout());

            folderLabelPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // Top, left, bottom, right padding


            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5); // Padding for each component

            // Project Name
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            JLabel projectNameLabel = new JLabel(project.getName());
            projectNameLabel.setForeground(Color.WHITE);
            projectNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            folderLabelPanel.add(projectNameLabel, gbc);

            // Completed
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.WEST;
            JLabel completedLabel = new JLabel("Completed: " + completedCount);
            completedLabel.setForeground(SUCCESS_COLOR);
            completedLabel.setFont(new Font("Arial", Font.BOLD, 12));
            folderLabelPanel.add(completedLabel, gbc);

            // Review
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel reviewLabel = new JLabel("Review: " + reviewCount);
            reviewLabel.setForeground(WARNING_COLOR);
            reviewLabel.setFont(new Font("Arial", Font.BOLD, 12));
            folderLabelPanel.add(reviewLabel, gbc);

            // In Progress
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.anchor = GridBagConstraints.WEST;
            JLabel inProgressLabel = new JLabel("In Progress: " + inProgressCount);
            inProgressLabel.setForeground(INFO_COLOR);
            inProgressLabel.setFont(new Font("Arial", Font.BOLD, 12));
            folderLabelPanel.add(inProgressLabel, gbc);

            // To Do
            gbc.gridx = 1;
            gbc.gridy = 2;
            gbc.anchor = GridBagConstraints.EAST;
            JLabel toDoLabel = new JLabel("To Do: " + toDoCount);
            toDoLabel.setForeground(ERROR_COLOR);
            toDoLabel.setFont(new Font("Arial", Font.BOLD, 12));
            folderLabelPanel.add(toDoLabel, gbc);

            // Total Time Spent
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            JLabel totalTimeSpentLabel = new JLabel("Total Time Spent: " + 0);
            totalTimeSpentLabel.setForeground(ERROR_COLOR);
            totalTimeSpentLabel.setFont(new Font("Arial", Font.BOLD, 12));
            folderLabelPanel.add(totalTimeSpentLabel, gbc);

            // Add mouse listener for selection
            folderLabelPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    selectedProjectId = project.getId();
                    populateProjects(projects);  // Refresh folders to update selection
                    showTasks(getTasksByProjectId(selectedProjectId));
                }
            });

            // Add the folder label panel to the folderPanel
            folderPanel.add(folderLabelPanel);
        }
        folderPanel.revalidate();
        folderPanel.repaint();
    }


    //
    //
    //
    //
    //






    public List<Task> getTasksByProjectId(int projectId) {
        Project selectedProject = projects.stream()
                .filter(p -> p.getId() == projectId)
                .findFirst()
                .orElse(null);

        if (selectedProject != null) {
            return selectedProject.getTasks();
        } else {
            return Collections.emptyList(); // Return an empty list if the project is not found
        }
    }
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //


    private void showTasks(List<Task> tasks) {
        String[] columnNames = {"Task", "Description", "Create Date", "Status", "Total Time"};
        Object[][] taskData = new Object[tasks.size()][5];

        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            taskData[i][0] = task;  // Store the Task object itself
            taskData[i][1] = task.getDescription();
            taskData[i][2] = task.getCreateDate().toString();
            taskData[i][3] = task.getStatus();
            taskData[i][4] = task.getTotalTime();
        }

        DefaultTableModel tableModel = new DefaultTableModel(taskData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 3; // Editable for Task and Status columns
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Task.class; // Custom class for task column
                return String.class;
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                if (column == 3) { // Status column
                    Task task = (Task) getValueAt(row, 0); // Assuming Task object is in column 0
                    String newStatus = (String) aValue;
                    task.setStatus(newStatus);
                    // Update the database with the new status
                    try {
                        int taskId = Integer.parseInt(task.getId());
                        updateTaskStatusInDatabase(taskId, newStatus);
                    } catch (NumberFormatException nfe) {
                        System.err.println("Failed to parse task ID: " + task.getId());
                    }
                    fireTableCellUpdated(row, column);
                } else {
                    super.setValueAt(aValue, row, column);
                }
            }
        };

        taskTable = new JTable(tableModel);

        // Set table header properties
        JTableHeader header = taskTable.getTableHeader();
        header.setPreferredSize(new Dimension(100, 40)); // Set header height
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(PRIMARY_COLOR); // Set header background color to black
                label.setForeground(Color.WHITE); // Set header text color to white
                label.setHorizontalAlignment(SwingConstants.CENTER); // Center-align header text
                label.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5)); // Add padding to header
                return label;
            }
        });

        // Set cell renderer for other columns
        taskTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(PRIMARY_COLOR); // Set cell background color to black
                label.setForeground(Color.WHITE); // Set cell text color to white
                label.setHorizontalAlignment(SwingConstants.CENTER); // Center-align cell text
                label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Add padding to cells
                return label;
            }
        });

        // Custom renderer and editor for the Task column
        taskTable.getColumnModel().getColumn(0).setCellRenderer(new TaskCellRenderer());
        taskTable.getColumnModel().getColumn(0).setCellEditor(new TaskCellEditor());

        // Custom renderer and editor for the Status column
        taskTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        taskTable.getColumnModel().getColumn(3).setCellEditor(new StatusCellEditor());

        // Set preferred widths and row height
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(300); // Adjust width for Task column
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Set width for Description column
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Set width for Status column
        taskTable.setRowHeight(40); // Set row height to accommodate buttons and labels

        // Set table properties
        taskTable.setBackground(PRIMARY_COLOR); // Set background color for empty areas of the table
        taskTable.setForeground(Color.WHITE); // Set default text color
        taskTable.setGridColor(SECONDARY_COLOR); // Set grid line color
        taskTable.setBorder(BorderFactory.createEmptyBorder());

        // Set the current task row index
        if (currentTask != null) {
            currentTaskRowIndex = tasks.indexOf(currentTask);
        } else {
            currentTaskRowIndex = -1; // No task selected
        }

        JScrollPane tableScrollPane = new JScrollPane(taskTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove scroll pane border
        tableScrollPane.getViewport().setBackground(PRIMARY_COLOR);
        SwingUtilities.invokeLater(() -> {
            taskPanelR.removeAll();
            taskPanelR.add(tableScrollPane, BorderLayout.CENTER);
            taskPanelR.revalidate();
            taskPanelR.repaint();
        });
    }



    // Custom cell renderer for Task column
    private class TaskCellRenderer extends JPanel implements TableCellRenderer {
        private JLabel taskLabel;
        private JButton startButton;
        private JButton stopButton;

        public TaskCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            taskLabel = new JLabel();
            startButton = new JButton();
            stopButton = new JButton();

            // Set the background color to black
            setBackground(PRIMARY_COLOR);
            taskLabel.setForeground(Color.WHITE); // Set text color to white for better contrast

            // Load icons (adjust paths and sizes as needed)
            ImageIcon startIcon = loadImageIcon("icons/small-start-icon.png", 30, 30);
            ImageIcon stopIcon = loadImageIcon("icons/small-pause-con.png", 30, 30);

            if (startIcon != null) {
                startButton.setIcon(startIcon);
            }
            if (stopIcon != null) {
                stopButton.setIcon(stopIcon);
            }

            // Remove button text and set preferred size
            startButton.setText(null);
            stopButton.setText(null);
            startButton.setPreferredSize(new Dimension(30, 30));
            stopButton.setPreferredSize(new Dimension(30, 30));
            startButton.setBorderPainted(false);  // Remove the button border
            stopButton.setBorderPainted(false);   // Remove the button border
            startButton.setOpaque(true);
            startButton.setContentAreaFilled(true);
            stopButton.setOpaque(true);
            stopButton.setContentAreaFilled(true);

            // Set button background color
            startButton.setBackground(PRIMARY_COLOR);
            stopButton.setBackground(PRIMARY_COLOR);

            // Initial state
            stopButton.setEnabled(false);

            add(startButton);
            add(stopButton);
            add(taskLabel);

            // Action listeners
            startButton.addActionListener(e -> {
                Task task = (Task) startButton.getClientProperty("task");
                if (task != null) {
                    startTimer(task);
                    task.setRunning(true);
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                }
            });

            stopButton.addActionListener(e -> {
                Task task = (Task) stopButton.getClientProperty("task");
                if (task != null) {
                    stopTimer();
                    task.setRunning(false);
                    stopButton.setEnabled(false);
                    startButton.setEnabled(true);
                }
            });
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Task task = (Task) value;
            taskLabel.setText(task.getName());
            startButton.setEnabled(!task.isRunning());
            stopButton.setEnabled(task.isRunning());
            startButton.setVisible(!task.isRunning());
            stopButton.setVisible(task.isRunning());

            // Store task reference in buttons
            startButton.putClientProperty("task", task);
            stopButton.putClientProperty("task", task);

            return this;
        }
    }

    private class StatusCellRenderer extends JPanel implements TableCellRenderer {
        private CustomStatusComboBox statusComboBox;

        public StatusCellRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER));
            statusComboBox = new CustomStatusComboBox();

            // Set the background color to black or primary color
//            setBackground(Color.green);
//            statusComboBox.setForeground(Color.blue); // Set text color to white for better contrast

            add(statusComboBox);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//            if (value instanceof String) {
//                statusComboBox.setSelectedItem(value);
//
//                // Get the Task object from the first column
//                Task task = (Task) table.getValueAt(row, 0);
//                statusComboBox.putClientProperty("task", task);
//            } else {
//                // Handle unexpected value types
//                System.err.println("Unexpected value type: " + (value != null ? value.getClass().getName() : "null"));
//                statusComboBox.setSelectedItem(null); // Clear selection or set to default
//                statusComboBox.putClientProperty("task", null);
//            }
            if (value instanceof String) {
                String status = (String) value;
                statusComboBox.setSelectedItem(status);
                setBackground(PRIMARY_COLOR);

                // Set the foreground color based on the status
                switch (status) {
                    case "Completed":
                        statusComboBox.setForeground(SUCCESS_COLOR);
                        break;
                    case "ToDo":
                        statusComboBox.setForeground(ERROR_COLOR);
                        break;
                    case "InProgress":
                        statusComboBox.setForeground(INFO_COLOR);
                        break;
                    case "Review":
                        statusComboBox.setForeground(WARNING_COLOR);
                        break;
                    default:
                        statusComboBox.setForeground(Color.WHITE); // Default color
                        break;
                }

                // Get the Task object from the first column
                Task task = (Task) table.getValueAt(row, 0);
                statusComboBox.putClientProperty("task", task);
            } else {
                // Handle unexpected value types
                System.err.println("Unexpected value type: " + (value != null ? value.getClass().getName() : "null"));
                statusComboBox.setSelectedItem(null); // Clear selection or set to default
                statusComboBox.putClientProperty("task", null);
            }

            // Handle selection and focus styles
//            if (isSelected) {
//                setBackground(Color.red);
//                statusComboBox.setForeground(Color.green);
//            } else {
//                setBackground(PRIMARY_COLOR);
////                statusComboBox.setForeground(Color.red); // Ensure consistent text color
//
//            }

            setOpaque(true);
            return this;
        }
    }


//    private class StatusCellRenderer extends JPanel implements TableCellRenderer {
//        private CustomStatusComboBox statusComboBox;
//
//        public StatusCellRenderer() {
//            setLayout(new FlowLayout(FlowLayout.LEFT));
////            statusComboBox = new JComboBox<>(new String[]{"ToDo", "InProgress", "Completed", "Review"});
//
//            // Set the background color to black
//            setBackground(PRIMARY_COLOR);
//            statusComboBox.setForeground(Color.WHITE); // Set text color to white for better contrast
//
//            add(statusComboBox);
//        }
//
//        @Override
//        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//            if (value instanceof String) {
//                statusComboBox.setSelectedItem(value);
//
//                // Get the Task object from the first column
//                Task task = (Task) table.getValueAt(row, 0);
//                statusComboBox.putClientProperty("task", task);
//            } else {
//                // Handle unexpected value types
//                System.err.println("Unexpected value type: " + (value != null ? value.getClass().getName() : "null"));
//                statusComboBox.setSelectedItem(null); // Clear selection or set to default
//                statusComboBox.putClientProperty("task", null);
//            }
//
//            // Handle selection and focus styles
//            if (isSelected) {
//                setBackground(table.getSelectionBackground());
//                statusComboBox.setForeground(table.getSelectionForeground());
//            } else {
//                setBackground(table.getBackground());
//                statusComboBox.setForeground(Color.WHITE); // Ensure consistent text color
//            }
//
//            setOpaque(true);
//            return this;
//        }
//    }


//    private class StatusCellRenderer extends JPanel implements TableCellRenderer {
//        private JComboBox<String> statusComboBox;
//
//        public StatusCellRenderer() {
//            setLayout(new FlowLayout(FlowLayout.LEFT));
//            statusComboBox = new JComboBox<>(new String[]{"ToDo", "InProgress", "Completed", "Review"});
//
//            // Set the background color to black
//            setBackground(PRIMARY_COLOR);
//            statusComboBox.setForeground(Color.WHITE); // Set text color to white for better contrast
//
//            add(statusComboBox);
//
//            statusComboBox.addActionListener(e -> {
//                System.out.println("ComboBox ActionListener invoked."); // Debug statement
//                Task task = (Task) statusComboBox.getClientProperty("task");
//                if (task != null) {
//                    String newStatus = (String) statusComboBox.getSelectedItem();
//                    System.out.println("Status changed for task: " + task.getName() + " to: " + newStatus);
//
//                    try {
//                        int taskId = Integer.parseInt(task.getId());
//                        System.out.println("Parsed task ID: " + taskId);
//                        task.setStatus(newStatus); // Update the task status in the application's state
//                        updateTaskStatusInDatabase(taskId, newStatus); // Update the task status in the database
//                    } catch (NumberFormatException nfe) {
//                        System.err.println("Failed to parse task ID: " + task.getId());
//                    }
//                } else {
//                    System.out.println("No task found in the ComboBox property.");
//                }
//            });
//        }
//
//        @Override
//        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//            if (value instanceof Task) {
//                Task task = (Task) value;
//                statusComboBox.setSelectedItem(task.getStatus());
//
//                // Store task reference in ComboBox
//                statusComboBox.putClientProperty("task", task);
//            } else {
//                // Handle unexpected value types
//                System.err.println("Unexpected value type: " + (value != null ? value.getClass().getName() : "null"));
//                statusComboBox.setSelectedItem(null); // Clear selection or set to default
//                statusComboBox.putClientProperty("task", null);
//            }
//
//            // Handle selection and focus styles
//            if (isSelected) {
//                setBackground(table.getSelectionBackground());
//                statusComboBox.setForeground(table.getSelectionForeground());
//            } else {
//                setBackground(table.getBackground());
//                statusComboBox.setForeground(Color.WHITE); // Ensure consistent text color
//            }
//
//            setOpaque(true);
//            return this;
//        }
//    }


    // Custom cell editor for Task column
    private class TaskCellEditor extends AbstractCellEditor implements TableCellEditor {
        private TaskCellRenderer taskRenderer;

        public TaskCellEditor() {
            taskRenderer = new TaskCellRenderer();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            taskRenderer.getTableCellRendererComponent(table, value, isSelected, true, row, column);
            return taskRenderer;
        }

        @Override
        public Object getCellEditorValue() {
            return null; // Return null since we don't need to modify the actual value in the table model
        }
    }

    private class StatusCellEditor extends AbstractCellEditor implements TableCellEditor {

        private CustomStatusComboBox comboBox;

        public StatusCellEditor() {
            comboBox = new CustomStatusComboBox();
            comboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped(); // Notify that editing is complete
                }
            });
        }

        @Override
        public Object getCellEditorValue() {
            return comboBox.getSelectedItem();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            comboBox.setSelectedItem(value);
            return comboBox;
        }
    }




    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //



    private void updateTaskStatusInDatabase(int taskId, String newStatus) {
        String sql = "UPDATE UserTasks SET status = ? WHERE task_id = ? AND user_id = ?";
        System.out.println("Calling updateTaskStatusInDatabase with taskId: " + taskId + " and newStatus: " + newStatus);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, taskId);
            pstmt.setInt(3, user.getUserId());

            int rowsUpdated = pstmt.executeUpdate();
            System.out.println("Update executed. Rows updated: " + rowsUpdated);
            populateProjects(projects);
            showTasks(getTasksByProjectId(selectedProjectId));

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQLException occurred while updating task status: " + e.getMessage());
        }
    }






    private void showAddTaskDialog() {
        AddTaskModal dialog = new AddTaskModal(this, selectedProjectId,user.getUserId());

        dialog.setVisible(true);

        // Debug statement to check the status
        String status = dialog.getStatus();
        System.out.println("Debug: Status returned from AddTaskModal: " + status);

        // Check if the task was successfully added
        if ("Success".equals(status)) {
            Task newTask = dialog.getCreatedTask();

            // Debug statement to check the created task
            System.out.println("Debug: Created Task: " + newTask);

            Project currentProject = projects.stream().filter(p -> p.getId()==selectedProjectId).findFirst().orElse(null);

            if (newTask != null) {
                currentProject.getTasks().add(newTask);
                showTasks(getTasksByProjectId(currentProject.getId()));
            }
        }
    }


    private void startTimer(Task task) {
//        initializeOrUpdateDatabase();
        System.out.println("Start timer: " + task.getName());

        stopTimer();  // Stop any existing timer
        currentTask = task;
        Project currentProject = projects.stream().filter(p -> p.getId()==selectedProjectId).findFirst().orElse(null);
        currentTaskDetailsLabel.setText("Time Spent: " + currentTask.getTimeSpent());
        currentTaskLabel.setText(currentProject.getName());

        // Set task as running
        task.setRunning(true);

        // Determine the row index of the current task
        currentTaskRowIndex = -1;
        for (int i = 0; i < taskTable.getRowCount(); i++) {
            Task t = (Task) taskTable.getValueAt(i, 0); // Assuming the task is in the first column
            if (t.equals(currentTask)) {
                currentTaskRowIndex = i;
                break;
            }
        }
        lastDatabaseUpdate = System.currentTimeMillis(); // Initialize the last update time

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> updateTimeSpent());
            }
        }, 1000, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }
        if (currentTask != null) {
            currentTask.setRunning(false);
        }
//        currentTask = null;
//        currentProject = null;
//        currentTaskDetailsLabel.setText("No task running");

        // Refresh task list or table to update button states
        showTasks(getTasksByProjectId(selectedProjectId));
    }


    private void updateTimeSpent() {
        if (currentTask != null) {
            currentTask.incrementTimeSpent();
            currentTaskDetailsLabel.setText(currentTask.getName() + " - Time Spent: " + currentTask.getTimeSpent());

            if (currentTaskRowIndex >= 0 && taskTable != null) {
                // Update the time spent in the table cell for the current task
                SwingUtilities.invokeLater(() -> {
//                    taskTable.getModel().setValueAt(currentTask.getTotalTime(), currentTaskRowIndex, 4); // Update "Total Time" column
                    DefaultTableModel model = (DefaultTableModel) taskTable.getModel();
                    model.setValueAt(currentTask.getTotalTime(), currentTaskRowIndex, 4); // Update "Total Time" column
                    taskTable.revalidate();  // Ensure table layout is updated
                    taskTable.repaint();
                });
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastDatabaseUpdate >= UPDATE_INTERVAL_MS) {
                try {
                    initializeOrUpdateDatabase();
                } catch (Exception e) {
                    System.err.println("Error updating database: " + e.getMessage());
                }
                lastDatabaseUpdate = currentTime;
            }

            updateStatusWindow();

        }
    }

    private int convertTimeStringToSeconds(String timeString) {
        try {
            String[] parts = timeString.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid time format. Expected HH:MM:SS.");
            }

            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);

            return hours * 3600 + minutes * 60 + seconds;
        } catch (NumberFormatException e) {
            System.err.println("Error converting time string to seconds: Invalid number format.");
            return 0; // Default to 0 if there is a number format error
        } catch (IllegalArgumentException e) {
            System.err.println("Error converting time string to seconds: " + e.getMessage());
            return 0; // Default to 0 if there is an illegal argument
        }
    }


    private void initializeOrUpdateDatabase() {
        try {
            Class.forName("org.sqlite.JDBC"); // Load the SQLite JDBC driver
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                if (conn != null) {
                    try (Statement stmt = conn.createStatement()) {
                        // Create the tasks table if it does not exist
                        String createTableSQL = "CREATE TABLE IF NOT EXISTS UserTasks (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                "user_id INTEGER," + // Added user_id column
                                "task_id INTEGER," + // Added task_id column
                                "status TEXT DEFAULT 'ToDo',"+
                                "time_stamp INTEGER," +
                                "is_running INTEGER," +
                                "time_spent INTEGER)";
                        stmt.execute(createTableSQL);
                        System.out.println("Database and tasks table initialized.");

                        if (currentTask != null) {
                            // Ensure currentTask is initialized and has an ID
                            String taskIdString = currentTask.getId();
                            int taskId;
                            try {
                                taskId = Integer.parseInt(taskIdString);
                            } catch (NumberFormatException e) {
                                System.err.println("Error converting task ID to integer: " + e.getMessage());
                                return; // Exit the method if the ID is invalid
                            }

                            int userId = user.getUserId(); // Retrieve the user ID

                            // Set all tasks to not running for the given user
                            String updateAllTasksSQL = "UPDATE UserTasks SET is_running = 0 WHERE user_id = ? AND is_running = 1";
                            try (PreparedStatement pstmt = conn.prepareStatement(updateAllTasksSQL)) {
                                pstmt.setInt(1, userId);
                                pstmt.executeUpdate();
                            }

                            // Check if the task already exists in the database for the given user
                            String checkTaskSQL = "SELECT COUNT(*) FROM UserTasks WHERE task_id = ? AND user_id = ?";
                            try (PreparedStatement checkPstmt = conn.prepareStatement(checkTaskSQL)) {
                                checkPstmt.setInt(1, taskId);
                                checkPstmt.setInt(2, userId);
                                try (ResultSet rs = checkPstmt.executeQuery()) {
                                    if (rs.next() && rs.getInt(1) > 0) {
                                        // Task exists, update the record
                                        updateTaskRecord(conn, taskId);
                                    } else {
                                        // Task does not exist, insert a new record
                                        insertTaskRecord(conn, taskId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error initializing or updating database: " + e.getMessage());
        }
    }


    private void updateTaskRecord(Connection conn,  int taskId) {
        try {
            String sql = "UPDATE UserTasks SET time_stamp = ?, is_running = ?, time_spent = ? WHERE user_id = ? AND task_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                long currentTimeStamp = System.currentTimeMillis() / 1000; // timestamp in seconds
                int timeSpent = convertTimeStringToSeconds(currentTask.getTimeSpent());

                pstmt.setLong(1, currentTimeStamp);
                pstmt.setInt(2, currentTask.isRunning() ? 1 : 0);
                pstmt.setInt(3, timeSpent);
                pstmt.setInt(4, user.getUserId()); // Set the user ID to identify the record to update
                pstmt.setInt(5, taskId); // Set the task ID to identify the record to update

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Database updated successfully.");
                } else {
                    System.out.println("No rows updated in the database. Check if the user ID and task ID combination exists.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating task record: " + e.getMessage());
        }
    }

    private void insertTaskRecord(Connection conn,int taskId) {
        try {
            // Check if the record already exists
            String checkSql = "SELECT COUNT(*) FROM UserTasks WHERE user_id = ? AND task_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, user.getUserId());
                checkStmt.setInt(2, taskId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        System.out.println("Record already exists for user ID " + user.getUserId() + " and task ID " + taskId);
                        return; // Exit the method if the record already exists
                    }
                }
            }

            // Insert new record
            String sql = "INSERT INTO UserTasks (user_id, task_id, time_stamp, is_running, time_spent) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                long currentTimeStamp = System.currentTimeMillis() / 1000; // timestamp in seconds

                pstmt.setInt(1, user.getUserId()); // Set user ID
                pstmt.setInt(2, taskId); // Set task ID
                pstmt.setLong(3, currentTimeStamp); // Set current timestamp
                pstmt.setInt(4, 0); // Set running status
                pstmt.setInt(5, 0); // Set timeSpent to 0

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Task record initialized in the database.");
                } else {
                    System.out.println("No rows inserted into the database.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting task record: " + e.getMessage());
        }
    }





    private void updateGlobalTimerInDatabase(long totalSeconds) {
        String sql = "UPDATE UserTime SET hours_worked_daily = ? WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, totalSeconds);
            pstmt.setInt(2, user.getUserId()); // replace userId with the actual user ID
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void startGlobalTimer() {
//        if (isGlobalTimerRunning) {
//            return;
//        }
//        if (currentTask != null) {
//            startTimer(currentTask);
//
//        }

        startIdleTracking();
        globalStartTime = System.currentTimeMillis() - elapsedTimeBeforeStop;;
        globalTimer = new Timer();
        globalTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> updateGlobalTimer());
            }
        }, 1000, 1000);
        isGlobalTimerRunning = true;
        lastDatabaseUpdateForGlobalTimer = System.currentTimeMillis();
        if (startIconGlobalTimer != null) {
            startIconGlobalTimer.setVisible(false);
        }

        if (stopIconGlobalTimer != null) {
            stopIconGlobalTimer.setVisible(true);
        }

    }




    private void startIdleTracking() {
        if (idleTimer != null) {
            idleTimer.cancel();
        }
        idleTimer = new Timer();
        idleTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long idleTime = listener.getIdleTime();
                System.out.println("Idle Time: " + idleTime / 1000 + " seconds");

                if (idleTime >= IDLE_TIME_TRACKING) {  // 15 seconds idle time
                    idleTimer.cancel();  // Stop further checks during the countdown
                    SwingUtilities.invokeLater(() -> showIdleAlert());
                }
            }
        }, 0, 5000); // Every 5 seconds
    }
    private void showIdleAlert() {
        JDialog idleDialog = new JDialog(this, "Idle Alert", true);
        idleDialog.setLayout(new BorderLayout());
        idleDialog.setSize(400, 300);
        idleDialog.setLocationRelativeTo(this);

        // Set the background color of the dialog
        idleDialog.getContentPane().setBackground(SECONDARY_COLOR);

        // Message label with padding from the top
        JLabel messageLabel = new JLabel("You have been idle for a while");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setForeground(SUCCESS_COLOR); // Set text color to SUCCESS_COLOR
        messageLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0)); // Add padding from top
        idleDialog.add(messageLabel, BorderLayout.NORTH);

        // Panel for countdown and seconds using BoxLayout for vertical alignment
        JPanel countdownPanel = new JPanel();
        countdownPanel.setBackground(SECONDARY_COLOR);
        countdownPanel.setLayout(new BoxLayout(countdownPanel, BoxLayout.Y_AXIS));

        // Countdown label with margin
        JLabel countdownLabel = new JLabel("Time remaining:");
        countdownLabel.setForeground(SUCCESS_COLOR); // Set text color to SUCCESS_COLOR
        countdownLabel.setFont(countdownLabel.getFont().deriveFont(Font.PLAIN, 16f)); // Regular font
        countdownLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally
        countdownLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 10, 0)); // Add margin to top and bottom
        countdownPanel.add(countdownLabel);

        // Seconds label with bold style and centered vertically
        JLabel secondsLabel = new JLabel("");
        secondsLabel.setForeground(SUCCESS_COLOR); // Set text color to SUCCESS_COLOR
        secondsLabel.setFont(secondsLabel.getFont().deriveFont(Font.BOLD, 28f)); // Bold font
        secondsLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally
        countdownPanel.add(Box.createVerticalGlue()); // Add space above
        countdownPanel.add(secondsLabel);
        countdownPanel.add(Box.createVerticalGlue()); // Add space below

        idleDialog.add(countdownPanel, BorderLayout.CENTER);

        // Create a panel for the button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(SECONDARY_COLOR); // Match background color with the dialog
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical alignment

        // Add some vertical spacing above the button
        buttonPanel.add(Box.createVerticalGlue());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(WARNING_COLOR); // Set background color to WARNING_COLOR
        cancelButton.setForeground(SUCCESS_COLOR); // Set text color to SUCCESS_COLOR
        int fixedHeight = 35; // Set the fixed height
        int fixedWidth = 120;  // Set the fixed width
        cancelButton.setPreferredSize(new Dimension(fixedWidth, fixedHeight));
        cancelButton.setMaximumSize(new Dimension(fixedWidth, fixedHeight)); // Ensure fixed size
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the button horizontally
        cancelButton.setMargin(new Insets(5, 10, 5, 10)); // Add padding to the text
        cancelButton.addActionListener(e -> {
            idleDialog.dispose();
            startIdleTracking(); // Restart idle tracking
        });

        buttonPanel.add(cancelButton);

        // Add some vertical spacing below the button
        buttonPanel.add(Box.createVerticalGlue());

        idleDialog.add(buttonPanel, BorderLayout.SOUTH);

        Timer countdownTimer = new Timer();
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            int remainingTime = 60;  // 60 seconds countdown

            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    if (remainingTime > 0) {
                        long idleTime = listener.getIdleTime();

                        if (idleTime < IDLE_TIME_TRACKING) {
                            countdownTimer.cancel();
                            idleDialog.dispose();
                            startIdleTracking();
                        }
                        secondsLabel.setText(remainingTime+"");  // Update only the seconds label
                        remainingTime--;
                    } else {
                        countdownTimer.cancel();
                        idleDialog.dispose();

                        long idleTime = listener.getIdleTime();

                        if (idleTime >= IDLE_TIME_TRACKING) {
                            stopGlobalTimer();  // Stop the global timer
                        }
                    }
                });
            }
        }, 1000, 1000); // Update every second

        idleDialog.setVisible(true);
    }




//    private void showIdleAlert() {
//        JDialog idleDialog = new JDialog(this, "Idle Alert", true);
//        idleDialog.setLayout(new BorderLayout());
//        idleDialog.setSize(300, 150);
//        idleDialog.setLocationRelativeTo(this);
//
//        JLabel messageLabel = new JLabel("You have been idle for a while. Timer will stop in 60 seconds.");
//        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
//        idleDialog.add(messageLabel, BorderLayout.CENTER);
//
//        JLabel countdownLabel = new JLabel("Time remaining: 60 seconds");
//        countdownLabel.setHorizontalAlignment(SwingConstants.CENTER);
//        idleDialog.add(countdownLabel, BorderLayout.NORTH);
//
//        JButton cancelButton = new JButton("Cancel");
//        cancelButton.addActionListener(e -> {
//            idleDialog.dispose();
//            startIdleTracking(); // Restart idle tracking
//        });
//        idleDialog.add(cancelButton, BorderLayout.SOUTH);
//
//        Timer countdownTimer = new Timer();
//        countdownTimer.scheduleAtFixedRate(new TimerTask() {
//            int remainingTime = 5;  // 60 seconds countdown
//
//            @Override
//            public void run() {
//                SwingUtilities.invokeLater(() -> {
//                    if (remainingTime > 0) {
//
//                        long idleTime = listener.getIdleTime();
//
//                        if (idleTime < IDLE_TIME_TRACKING) {
////                            idleTimer.cancel();  // Stop further checks during the countdown
////                            stopGlobalTimer();  // Stop the global timer
//                            countdownTimer.cancel();
//                            idleDialog.dispose();
//                            startIdleTracking();
//                        }
//                        countdownLabel.setText("Time remaining: " + (--remainingTime) + " seconds");
//
//                    } else {
//                        countdownTimer.cancel();
//                        idleDialog.dispose();
//
//                        long idleTime = listener.getIdleTime();
//
//                        if (idleTime >= IDLE_TIME_TRACKING) {
////                            idleTimer.cancel();  // Stop further checks during the countdown
//                            stopGlobalTimer();  // Stop the global timer
//
//                        }
//
//
//                    }
//                });
//            }
//        }, 1000, 1000); // Update every second
//
//        idleDialog.setVisible(true);
//    }

    private void stopGlobalTimer() {
        if (globalTimer != null) {
            globalTimer.cancel();
            globalTimer = null;
        }
        elapsedTimeBeforeStop = System.currentTimeMillis() - globalStartTime;

        isGlobalTimerRunning = false;
        if (stopIconGlobalTimer != null) {
            stopIconGlobalTimer.setVisible(false);
        }

        if (startIconGlobalTimer != null) {
            startIconGlobalTimer.setVisible(true);
        }

        if (currentTask != null) {
            stopTimer();
        }
    }

    private void updateGlobalTimer() {
        long elapsedTime = System.currentTimeMillis() - globalStartTime;
        int seconds = (int) (elapsedTime / 1000) % 60;
        int minutes = (int) (elapsedTime / 60000) % 60;
        int hours = (int) (elapsedTime / 3600000);
        globalTimerLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDatabaseUpdateForGlobalTimer >= UPDATE_INTERVAL_MS) {
            try {
                long totalSeconds = elapsedTime / 1000;
                updateGlobalTimerInDatabase(totalSeconds);
                user.setTimeSpent((int) totalSeconds);

            } catch (Exception e) {
                System.err.println("Error updating database: " + e.getMessage());
            }
            lastDatabaseUpdateForGlobalTimer = currentTime;
        }
    }


    private void onWindowResize() {
        int frameWidth = getWidth();
//        folderScrollPane.setBounds(20, 100, frameWidth - 40, 100);
        taskScrollPane.setBounds(20, 250, frameWidth - 40, getHeight() - 310);
        addButton.setBounds(frameWidth - 130, 10, 120, 30);
//        currentTaskPanel.setBounds(20, 10, frameWidth - 180, 80);
    }

}