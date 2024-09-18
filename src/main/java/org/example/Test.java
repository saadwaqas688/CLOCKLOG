package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;

public class Test {

    // List of browser processes to check (exact names as they appear in ps output)
    private static final String[] BROWSER_PROCESSES = {
            "chrome",        // Google Chrome
            "firefox",       // Mozilla Firefox
            "microsoft-edge", // Microsoft Edge
            "opera",         // Opera
            "safari"         // Safari (macOS)
    };

    public static void main(String[] args) {
        while (true) {
            detectBrowsers();
            try {
                // Sleep for 5 seconds before checking again
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void detectBrowsers() {
        Set<String> detectedBrowsers = new HashSet<>();

        try {
            // Execute the system command to get the list of running processes
            Process process = Runtime.getRuntime().exec("ps -e");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            boolean browserFound = false;

            // Read each line from the process output
            while ((line = reader.readLine()) != null) {
                // Split the process information by spaces (ps output format)
                String[] processDetails = line.trim().split("\\s+");
                if (processDetails.length > 3) {
                    // Get the actual process name from the last part
                    String processName = processDetails[3];

                    // Check if the process name matches any browser
                    for (String browser : BROWSER_PROCESSES) {
                        if (processName.equals(browser) && !detectedBrowsers.contains(browser)) {
                            System.out.println(browser + " is running.");
                            detectedBrowsers.add(browser);  // Add to set to avoid duplicate printing
                            browserFound = true;

                            // Check if the detected browser is Firefox
                            if (processName.equals("firefox")) {
                                // Display a warning popup if Firefox is detected
                                showWarningPopup();
                            }
                        }
                    }
                }
            }

            if (!browserFound) {
                System.out.println("No browsers are running.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to show the warning popup
    private static void showWarningPopup() {
        JOptionPane.showMessageDialog(null, "You are using a restricted browser (Firefox). Please close it.", "Restricted Browser Detected", JOptionPane.WARNING_MESSAGE);
    }
}
