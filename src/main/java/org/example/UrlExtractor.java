package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlExtractor extends JFrame {

    private static final long serialVersionUID = 1L;
    private JLabel statusLabel;
    private JButton startButton, stopButton;
    private ScheduledExecutorService scheduler;

    private static final String LOG_FILE = DatabaseConfig.getUrlLogFilePath();

    private static final Set<String> seenUrls = new HashSet<>();

    public UrlExtractor() {
        startTakingScreenshots();

//        super("Screenshot App");
//        initializeUI();
    }


    private Rectangle getBrowserUrlBarRectangle(Rectangle screenBounds) {
        // Define fixed dimensions for the URL bar rectangle
        int fixedWidth = 400;  // Example fixed width for the URL bar
        int fixedHeight = 40;  // Example fixed height for the URL bar

        // Define fixed coordinates for the URL bar from the top of the screen

        int fixedX = screenBounds.x+180; // Start at the left edge of the screen
        int fixedY = screenBounds.y+40; // Start at the top edge of the screen

        // Create the rectangle for the URL bar
        Rectangle browserUrlBarRect = new Rectangle(fixedX, fixedY, fixedWidth, fixedHeight);

        // Ensure the rectangle is within the bounds of the screen
        return browserUrlBarRect.intersection(screenBounds);
    }

    private void initializeUI() {
        setLayout(new FlowLayout());
        setSize(300, 120);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        startTakingScreenshots();


    }

    private void startTakingScreenshots() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                takeScreenshotAndProcess();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void takeScreenshotAndProcess() {
        try {
            // Get all the screens (GraphicsDevices)
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();

            File screenshotsDir = new File("screenshots");
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdirs();
            }

            for (int i = 0; i < screens.length; i++) {
                GraphicsDevice screen = screens[i];
                GraphicsConfiguration gc = screen.getDefaultConfiguration();
                Rectangle screenBounds = gc.getBounds();

                // Create a Robot to capture the screen
                Robot robot = new Robot();

                // Use the dynamic rectangle for the URL bar based on the current screen's size
                Rectangle browserUrlBarRect = getBrowserUrlBarRectangle(screenBounds);

                // Ensure the rectangle is within the screen bounds
                browserUrlBarRect = browserUrlBarRect.intersection(screenBounds);

                // Capture the screenshot of the specified area
                BufferedImage screenCapture = robot.createScreenCapture(browserUrlBarRect);


                // Save the screenshot to the screenshots directory
                File screenshotFile = new File(screenshotsDir, "screenshot_" + i + ".png");
                ImageIO.write(screenCapture, "png", screenshotFile);
                System.out.println("Screenshot saved: " + screenshotFile.getAbsolutePath());



                // Process the image directly without saving
                String extractedText = extractTextFromImage(screenCapture);
                System.out.println("Extracted Text from screen " + i + ": \n" + extractedText);

                // Extract and log URLs
                extractAndLogURLs(extractedText);
//                logURLToFile(extractedText);
            }

        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }


//    private void takeScreenshotAndProcess() {
//        try {
//            // Create screenshots directory if it does not exist
//            File screenshotsDir = new File("screenshots");
//            if (!screenshotsDir.exists()) {
//                screenshotsDir.mkdir();
//            }
//
//            // Get all the screens (GraphicsDevices)
//            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//            GraphicsDevice[] screens = ge.getScreenDevices();
//
//            for (int i = 0; i < screens.length; i++) {
//                GraphicsDevice screen = screens[i];
//                GraphicsConfiguration gc = screen.getDefaultConfiguration();
//                Rectangle screenBounds = gc.getBounds();
//
//                // Create a Robot to capture the screen
//                Robot robot = new Robot();
//
//                // Use the dynamic rectangle for the URL bar based on the current screen's size
//                Rectangle browserUrlBarRect = getBrowserUrlBarRectangle(screenBounds);
//
//                // Ensure the rectangle is within the screen bounds
//                browserUrlBarRect = browserUrlBarRect.intersection(screenBounds);
//
//                // Capture the screenshot of the specified area
//                BufferedImage screenCapture = robot.createScreenCapture(browserUrlBarRect);
//
//                // Save the image to the screenshots directory
//                File outputfile = new File(screenshotsDir, "screenshot_" + i + ".png");
//                ImageIO.write(screenCapture, "png", outputfile);
//
//
//
//                // Process the image
//                String extractedText = extractTextFromImage(outputfile);
//                System.out.println("Extracted Text from screen " + i + ": \n" + extractedText);
//
//                // Extract and log URLs
//                extractAndLogURLs(extractedText);
//            }
//
//        } catch (AWTException | IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private String extractTextFromImage(BufferedImage image) {
//        ITesseract instance = new Tesseract();
//        try {
//            // Specify the directory path for the tessdata folder
//            String tessDataPath = getClass().getClassLoader().getResource("tessdata").getPath();
//
//            instance.setDatapath(tessDataPath);
//            return instance.doOCR(image);
//        } catch (TesseractException e) {
//            e.printStackTrace();
//            return "Error occurred while extracting text.";
//        }
//    }


    private String extractTextFromImage(BufferedImage image) {
        ITesseract instance = new Tesseract();
        try {
            // Use a more reliable method to get the resource URL
            URL tessDataUrl = getClass().getClassLoader().getResource("tessdata");

            if (tessDataUrl == null) {
                throw new IllegalStateException("Cannot find tessdata folder");
            }

            String tessDataPath;

            // Check if the resource is inside a JAR or not
            if (tessDataUrl.getProtocol().equals("jar")) {
                // Extract the tessdata folder to a temporary directory
                File tempDir = Files.createTempDirectory("tessdata").toFile();
                extractResourceFolder("/tessdata", tempDir.getAbsolutePath());
                tessDataPath = tempDir.getAbsolutePath();
            } else {
                // It's not inside a JAR, so we can use the path directly
                tessDataPath = tessDataUrl.getPath();
            }

            instance.setDatapath(tessDataPath);
            return instance.doOCR(image);
        } catch (TesseractException | IOException e) {
            e.printStackTrace();
            return "Error occurred while extracting text.";
        }
    }

    private void extractResourceFolder(String resourcePath, String destPath) throws IOException {
        URL url = getClass().getResource(resourcePath);
        if (url == null) {
            throw new FileNotFoundException("Resource not found: " + resourcePath);
        }
        File destDir = new File(destPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        try (JarFile jar = new JarFile(new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()))) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.startsWith(resourcePath.substring(1))) {
                    File file = new File(destDir, entryName.replace(resourcePath.substring(1), ""));
                    if (entry.isDirectory()) {
                        file.mkdirs();
                    } else {
                        try (InputStream is = jar.getInputStream(entry)) {
                            Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        } catch (URISyntaxException e) {
            throw new IOException("Failed to extract resource folder", e);
        }
    }


    private void extractAndLogURLs(String text) {
        // Regular expression to match domain names
        String domainRegex = "\\b(?!\\d)[a-zA-Z0-9-]{2,}\\.[a-zA-Z]{2,}(?:\\/[^\\s]*)?\\b";
        String partialDomainRegex = "\\b(?!\\d)[a-zA-Z0-9-]+(com|org|net|edu|gov|pk)\\b";

        Pattern domainPattern = Pattern.compile(domainRegex);
        Pattern partialDomainPattern = Pattern.compile(partialDomainRegex);

        Matcher domainMatcher = domainPattern.matcher(text);
        Matcher partialDomainMatcher = partialDomainPattern.matcher(text);

        Set<String> domains = new HashSet<>();
        while (domainMatcher.find()) {
            domains.add(domainMatcher.group());
        }

        while (partialDomainMatcher.find()) {
            domains.add(partialDomainMatcher.group());
        }

        // Process and log URLs
        for (String domain : domains) {
            String url = domain;
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            logURLToFile(url);
        }
    }

    private void logURLToFile(String url) {
        if (seenUrls.contains(url)) {
            System.out.println("Duplicate URL found: " + url);
            return;
        }

        seenUrls.add(url);

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(url);
            System.out.println("Logged URL: " + url);
        } catch (IOException e) {
            System.err.println("An error occurred while logging the URL: " + e.getMessage());
        }
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                new ScreenshotApp().setVisible(true);
//            }
//        });
//    }
}
