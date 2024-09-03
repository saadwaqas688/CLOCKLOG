package org.example;


import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseMotionListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;


public class GlobalEventListener implements NativeKeyListener, NativeMouseMotionListener {

    //    private static final String LOG_FILE = "user_activity.log";
//    private static final String SCREENSHOT_DIR = "screenshots";
    private static final String LOG_FILE = DatabaseConfig.getLogFilePath();
    private static final String SCREENSHOT_DIR = DatabaseConfig.getScreenshotDirPath();
    private static final long SCREEN_SHOT_TAKING_TIME = 120000;

    private boolean isMouseMoving = false;
    private final long MOVEMENT_THRESHOLD = 100; // Time in milliseconds to consider mouse movement as stopped
    private int mouseMoveCount = 0;
    private Timer movementTimer;


    private BufferedWriter writer;
    private int keyPressCount = 0;
    private long lastActivityTime = System.currentTimeMillis();
    private boolean isScreenShotNotification;


    public GlobalEventListener(boolean isScreenShotNotification) {

        try {
            this.isScreenShotNotification=isScreenShotNotification;
            openLogFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void openLogFile() throws IOException {
        writer = new BufferedWriter(new FileWriter(LOG_FILE, true)); // Append to existing file
    }

    public void closeLogFile() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    private void logEvent(String message) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());
        writer.write(timestamp + ": " + message + "\n");
        writer.flush();
    }

    public void startPeriodicLogging() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    logEvent(getCounts() + ", Focused Window: " + getFocusedWindowTitle());
                    takeScreenshots();

//                    mouseMoveCount = 0; // Reset the count after logging
//                    keyPressCount = 0;  // Reset the count after logging
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, SCREEN_SHOT_TAKING_TIME); // 15000 ms = 15 seconds
    }


    private String getFocusedWindowTitle() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return getFocusedWindowTitleWindows();
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            return getFocusedWindowTitleLinux();
        }
        return "Unknown";
    }

    private String getFocusedWindowTitleLinux() {
        try {
            // Run xprop to get the ID of the currently active window
            Process process = Runtime.getRuntime().exec("xprop -root _NET_ACTIVE_WINDOW");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();

            if (line != null) {
                // Extract window ID from the output
                String windowId = line.split(" ")[line.split(" ").length - 1];

                // Run xprop to get the name of the window using its ID
                Process windowProcess = Runtime.getRuntime().exec("xprop -id " + windowId + " WM_NAME");
                BufferedReader windowReader = new BufferedReader(new InputStreamReader(windowProcess.getInputStream()));
                String windowTitle = windowReader.readLine();

                if (windowTitle != null) {
                    String title = windowTitle.split("=")[1].trim().replace("\"", "");
                    String[] parts = title.split(" - ");
                    return parts[0].trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private String getFocusedWindowTitleWindows() {
        try {
            User32 user32 = User32.INSTANCE;
            WinDef.HWND hwnd = user32.GetForegroundWindow();
            int length = user32.GetWindowTextLength(hwnd);
            char[] windowTitle = new char[length + 1];
            user32.GetWindowText(hwnd, windowTitle, length + 1);
            return new String(windowTitle).trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private synchronized void resetIdleState() {
        lastActivityTime = System.currentTimeMillis();
        mouseMoveCount = 0;
        keyPressCount = 0;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        synchronized (this) {
            keyPressCount++;
            lastActivityTime = System.currentTimeMillis();
        }
    }


    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        long currentTime = System.currentTimeMillis();

        synchronized (this) {
            // Check if the mouse was not moving or has stopped moving for a certain threshold
            if (!isMouseMoving || (currentTime - lastActivityTime > MOVEMENT_THRESHOLD)) {
                mouseMoveCount++; // Increment the movement count
                isMouseMoving = true; // Set the flag indicating the mouse is moving

                // Start or restart the timer
                if (movementTimer != null) {
                    movementTimer.cancel(); // Stop any existing timer
                }
                startMovementTimer();
            }

            lastActivityTime = currentTime; // Update the last activity time
        }
    }


    private void startMovementTimer() {
        movementTimer = new Timer(true); // Run as a daemon thread
        movementTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkMouseStop();
            }
        }, MOVEMENT_THRESHOLD, MOVEMENT_THRESHOLD);
    }

    private void checkMouseStop() {
        long currentTime = System.currentTimeMillis();

        synchronized (this) {
            // Check if the mouse has stopped moving for longer than the threshold
            if (currentTime - lastActivityTime > MOVEMENT_THRESHOLD) {
                isMouseMoving = false; // Reset the flag indicating the mouse has stopped moving
                movementTimer.cancel(); // Stop the timer as mouse has stopped moving
                movementTimer = null; // Nullify the timer reference to indicate it's stopped

                // Additional logic can be added here if needed when the mouse stops moving
                System.out.println("Mouse has stopped moving. Count so far: " + mouseMoveCount);
            }
        }
    }

    // Ensure to stop the timer when it's no longer needed, to avoid resource leaks
    public void stopTracking() {
        if (movementTimer != null) {
            movementTimer.cancel();
        }
    }


    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // Optional handling for key release events
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        // Optional handling for key typed events
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {
        // Optional handling for mouse drag events
    }



    // Method to calculate idle time
    public long getIdleTime() {
        return System.currentTimeMillis() - lastActivityTime;
    }

    // Method to get the total counts of mouse movements and key presses
    public synchronized String getCounts() {
        return "Mouse Moves: " + mouseMoveCount + ", Key Presses: " + keyPressCount;
    }


    public void setIsScreenShotNotification(boolean screenShotNotificationValue) {
        isScreenShotNotification = screenShotNotificationValue;
    }


    private void takeScreenshots() {
        try {
            File screenshotDir = new File(SCREENSHOT_DIR);
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();
            for (int i = 0; i < screens.length; i++) {
                GraphicsDevice screen = screens[i];
                Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();

                Robot robot = new Robot(screen);
                BufferedImage screenshot = robot.createScreenCapture(screenBounds);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String timestamp = dateFormat.format(new Date());
                File outputfile = new File(SCREENSHOT_DIR + File.separator + "screenshot_" + timestamp + "_screen" + i + ".png");

                ImageIO.write(screenshot, "png", outputfile);
            }
            if(isScreenShotNotification)
            SwingUtilities.invokeLater(() -> NotificationUtils.showNotification("Screenshots taken successfully!"));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

