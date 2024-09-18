package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;



public class UrlReceiverServer {

    private static final String LOG_FILE = DatabaseConfig.getUrlLogFilePath();
    private  long counter = 0;
    private static final String[] BROWSER_PROCESSES = {
            "chrome",        // Google Chrome
            "firefox",       // Mozilla Firefox
            "microsoft-edge", // Microsoft Edge
            "opera",         // Opera
            "safari"         // Safari (macOS)
    };



    private static List<String> detectedBrowsersList = new ArrayList<>();

    private static List<String> browsersListFromExtention = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UrlReceiverServer::new);
    }

    public UrlReceiverServer() {
        // JFrame frame = new JFrame("URL Receiver");
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setSize(400, 300);

        // textArea = new JTextArea();
        // textArea.setEditable(false);
        // JScrollPane scrollPane = new JScrollPane(textArea);

        // frame.add(scrollPane, BorderLayout.CENTER);
        // frame.setVisible(true);

        startServer();
        adjustBrowserProcessesForOS();

    }


        // Method to detect running browsers
    // private void detectBrowsers() {
    //     Set<String> detectedBrowsers = new HashSet<>();

    //     try {
    //         // Execute the system command to get the list of running processes
    //         Process process = Runtime.getRuntime().exec("ps -e");
    //         BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    //         String line;

    //         boolean browserFound = false;

    //         // Read each line from the process output
    //         while ((line = reader.readLine()) != null) {
    //             // Split the process information by spaces (ps output format)
    //             String[] processDetails = line.trim().split("\\s+");
    //             if (processDetails.length > 3) {
    //                 // Get the actual process name from the last part
    //                 String processName = processDetails[3];

    //                 // Check if the process name matches any browser
    //                 for (String browser : BROWSER_PROCESSES) {
    //                     if (processName.equals(browser) && !detectedBrowsers.contains(browser)) {
    //                         System.out.println(browser + " is running.");
    //                         detectedBrowsers.add(browser);  // Add to set to avoid duplicate printing
    //                         browserFound = true;

    //                         if (!detectedBrowsersList.contains(browser)) {
    //                             detectedBrowsersList.add(browser);
    //                             System.out.println("Added " + browser + " to detected browsers list: " + detectedBrowsersList);
    //                         }

    //                         // Show a warning popup if Firefox is detected

    //                         if(!browsersListFromExtention.contains(browser)){
    //                           showWarningPopup(browser);
    //                         }else{
    //                             browsersListFromExtention.remove(browser);
    //                         }
    //                         // if (processName.equals("firefox")) {
    //                         //     showWarningPopup();
    //                         // }
    //                     }
    //                 }
    //             }
    //         }

    //         if (!browserFound) {
    //             System.out.println("No browsers are running.");
    //         }


    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }


        // Adjust browser processes for Windows to include .exe extensions
    private void adjustBrowserProcessesForOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Add .exe extension for Windows browsers
            for (int i = 0; i < BROWSER_PROCESSES.length; i++) {
                BROWSER_PROCESSES[i] += ".exe";
            }
        }
    }


    private void detectBrowsers() {
        Set<String> detectedBrowsers = new HashSet<>();
        String os = System.getProperty("os.name").toLowerCase(); // Detect the OS

        try {
            Process process;
            if (os.contains("win")) {
                // Windows: Use "tasklist" to get the list of running processes
                process = Runtime.getRuntime().exec("tasklist");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Linux or macOS: Use "ps -e" to get the list of running processes
                process = Runtime.getRuntime().exec("ps -e");
            } else {
                throw new UnsupportedOperationException("Unsupported operating system: " + os);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean browserFound = false;

            // Read each line from the process output
            while ((line = reader.readLine()) != null) {
                String processName;

                if (os.contains("win")) {
                    // Windows tasklist format: extract the process name (column 0)
                    String[] processDetails = line.trim().split("\\s+");
                    processName = processDetails[0]; // Process name is the first column in Windows
                } else {
                    // Linux/macOS ps -e format: extract the process name from the last part
                    String[] processDetails = line.trim().split("\\s+");
                    processName = processDetails[processDetails.length - 1]; // Last column for process name
                }

                // Check if the process name matches any browser
                for (String browser : BROWSER_PROCESSES) {
                    if (processName.toLowerCase().contains(browser) && !detectedBrowsers.contains(browser)) {
                        System.out.println(browser + " is running.");
                        detectedBrowsers.add(browser); // Add to set to avoid duplicate printing
                        browserFound = true;

                        if (!detectedBrowsersList.contains(browser)) {
                            detectedBrowsersList.add(browser);
                            System.out.println("Added " + browser + " to detected browsers list: " + detectedBrowsersList);
                        }

                        // Show warning popup if browser is detected, unless it's already in the extension list
                        if (!browsersListFromExtention.contains(browser)) {
                            showWarningPopup(browser);
                        } else {
                            browsersListFromExtention.remove(browser);
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
    private void showWarningPopup(String browser) {
        JOptionPane.showMessageDialog(null, "You are using"+" "+browser+" "+"with out clocklog extention. Please close it.", "Restricted Browser Detected", JOptionPane.WARNING_MESSAGE);
    }


        public long getServerCounter() {
        return counter;
    }

public void setServerCounter() {
    // Increment the counter by 1
    this.counter++;
    
    // If counter reaches 10, reset it to 0
    if (counter == 15) {
        counter = 0;
         detectBrowsers();
    }

}


 
    

    private void startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(5025), 0);
            server.createContext("/receive-url", new UrlHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
            // appendText("Server is running on http://localhost:8080/receive-url\n");
        } catch (IOException e) {
            e.printStackTrace();
            // appendText("Failed to start server: " + e.getMessage() + "\n");
        }
    }

    // private void appendText(String text) {
    //     SwingUtilities.invokeLater(() -> textArea.append(text));
    // }

    private void logURLToFile(String url) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(LOG_FILE, true)))) {
            out.println(url);
        } catch (IOException e) {
                        e.printStackTrace();

            // appendText("Failed to log URL: " + e.getMessage() + "\n");
        }
    }

    class UrlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Handle CORS preflight request
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Handle POST requests
            if ("POST".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            // Parse JSON to extract "browser" and "url"
            JSONObject jsonObject = new JSONObject(requestBody);
            String browser = jsonObject.getString("browser");
            String url = jsonObject.getString("url");

             if (!browsersListFromExtention.contains(browser)) {

                    browsersListFromExtention.add(browser);
                 }


                System.out.println("browser list " +  detectedBrowsersList);

                // Log the received URL to the file
                logURLToFile(requestBody);

                String response = "{\"status\":\"success\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);

                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        }
    }
}
