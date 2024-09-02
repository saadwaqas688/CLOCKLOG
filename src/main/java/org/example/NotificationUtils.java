package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NotificationUtils {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 50;
    private static final int HORIZONTAL_PADDING = 10;
    private static final int VERTICAL_PADDING = 60;
    public static final Color PRIMARY_COLOR = new Color(3, 50, 70); // color of main window
    public static final Color SUCCESS_COLOR = new Color(17, 235, 130);//Completed
    private static final int ANIMATION_DELAY = 20; // milliseconds
    private static final int ANIMATION_STEPS = 100; // number of steps for the animation

    public static void showNotification(String message) {
        JFrame notificationFrame = new JFrame();
        notificationFrame.setUndecorated(true);
        notificationFrame.setSize(WIDTH, HEIGHT);
        notificationFrame.setLayout(new BorderLayout());
        notificationFrame.setOpacity(0.9f);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());

        // Create a label with the message and set the text color to green
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setForeground(SUCCESS_COLOR); // Set text color to green

        messagePanel.add(messageLabel, BorderLayout.CENTER);
        messagePanel.setBackground(PRIMARY_COLOR); // Set background color to primary color

        notificationFrame.add(messagePanel, BorderLayout.CENTER);

        // Get the main screen (primary monitor)
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] screens = ge.getScreenDevices();
        Rectangle mainScreenBounds = ge.getDefaultScreenDevice().getDefaultConfiguration().getBounds();

        // Calculate position for bottom-right corner of the main screen
        int x = mainScreenBounds.x + mainScreenBounds.width - WIDTH - HORIZONTAL_PADDING;
        int y = mainScreenBounds.y + mainScreenBounds.height - HEIGHT - VERTICAL_PADDING;

        notificationFrame.setLocation(x, y);

        // Display the notification
        notificationFrame.setVisible(true);

        // Start animation to vanish the frame to the left
        Timer animationTimer = new Timer(ANIMATION_DELAY, null);
        animationTimer.addActionListener(new ActionListener() {
            int step = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (step < ANIMATION_STEPS) {
                    // Calculate the new x position and opacity
                    float progress = (float) step / ANIMATION_STEPS;
                    int newX = (int) (x - (progress * WIDTH));
                    float newOpacity = 0.9f * (1.0f - progress);

                    // Update frame position and opacity
                    notificationFrame.setLocation(newX, y);
                    notificationFrame.setOpacity(newOpacity);

                    step++;
                } else {
                    // Stop the timer and dispose of the frame
                    animationTimer.stop();
                    notificationFrame.dispose();
                }
            }
        });
        animationTimer.start();
    }
}
