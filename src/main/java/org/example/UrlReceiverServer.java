package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

public class UrlReceiverServer {

    private static final String LOG_FILE = DatabaseConfig.getUrlLogFilePath();
    // private JTextArea textArea;

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
                // appendText("Received URL: " + requestBody + "\n");

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
