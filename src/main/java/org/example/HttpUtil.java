package org.example;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {

    // Method for sending POST requests
//    public static void sendPostRequest(String urlString, String jsonPayload) {
//        try {
//            URL url = new URL(urlString);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setRequestProperty("Accept", "application/json");
//            connection.setDoOutput(true);
//
//            try (OutputStream os = connection.getOutputStream()) {
//                byte[] input = jsonPayload.getBytes();
//                os.write(input, 0, input.length);
//            }
//
//            int code = connection.getResponseCode();
//            System.out.println("Response Code: " + code);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public static String sendPostRequest(String urlString, String jsonPayload) {
//        StringBuilder response = new StringBuilder();
//        try {
//            URL url = new URL(urlString);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setRequestProperty("Accept", "application/json");
//            connection.setDoOutput(true);
//
//            try (OutputStream os = connection.getOutputStream()) {
//                byte[] input = jsonPayload.getBytes();
//                os.write(input, 0, input.length);
//            }
//
//            int code = connection.getResponseCode();
//            System.out.println("Response Code: " + code);
//
//            // Read the response
//            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
//                String line;
//                while ((line = br.readLine()) != null) {
//                    response.append(line);
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return response.toString();
//    }

    public static JSONObject sendPostRequest(String urlString, String jsonPayload) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            // Write the JSON payload to the output stream
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes();
                os.write(input, 0, input.length);
            }

            // Get the response code
            int code = connection.getResponseCode();
            System.out.println("Response Code: " + code);

            // Read the response from the input stream
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convert response to JSONObject and return
        return new JSONObject(response.toString());
    }
    // Method for sending GET requests
    public static String sendGetRequest(String urlString) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Read response
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } else {
                System.out.println("GET request failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response.toString();
    }

    public static void main(String[] args) {
        // Example usage
        String apiUrl = "http://localhost:3000/getProjects";
        String response = sendGetRequest(apiUrl);
        System.out.println(response);
    }
}