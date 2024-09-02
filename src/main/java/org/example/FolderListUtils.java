package org.example;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class FolderListUtils {

    public static JLabel createUserAvatar(String imagePath, String userName) {
        CircularAvatarLabel avatarLabel = new CircularAvatarLabel();

        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setVerticalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(50, 50));
        avatarLabel.setFont(new Font("Arial", Font.BOLD, 24));
        avatarLabel.setOpaque(false);

        try {
            // Load the image from the resources folder
            java.net.URL imgUrl = FolderListUtils.class.getClassLoader().getResource(imagePath);
            if (imgUrl != null) {
                BufferedImage image = ImageIO.read(imgUrl);
                if (image != null) {
                    ImageIcon icon = new ImageIcon(image.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    avatarLabel.setIcon(icon);
                    avatarLabel.setText(""); // Clear text if image is loaded
                } else {
                    throw new IOException("Image not found");
                }
            } else {
                throw new IOException("Image URL not found");
            }
        } catch (IOException e) {
            // Image not found or cannot be read, display user's initial
            String initial = userName.length() > 0 ? userName.substring(0, 1).toUpperCase() : "?";
            avatarLabel.setText(initial);
        }

        return avatarLabel;
    }

    // Inner class to create a circular JLabel
    private static class CircularAvatarLabel extends JLabel {
        private static final int DIAMETER = 50;

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Clip to circular shape
            Shape circle = new Ellipse2D.Float(0, 0, getWidth(), getHeight());
            g2.setClip(circle);

            // Draw the background with a circular shape
            g2.setColor(getBackground());
            g2.fill(circle);

            // Draw the image or text
            if (getIcon() != null) {
                // Ensure the image is drawn within the circle
                Icon icon = getIcon();
                int iconWidth = icon.getIconWidth();
                int iconHeight = icon.getIconHeight();
                int x = (getWidth() - iconWidth) / 2;
                int y = (getHeight() - iconHeight) / 2;

                // Draw the image centered
                g2.drawImage(((ImageIcon) icon).getImage(), x, y, iconWidth, iconHeight, null);
            } else {
                g2.setFont(getFont());
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getAscent();
                int textX = (getWidth() - textWidth) / 2;
                int textY = (getHeight() + textHeight) / 2 - fm.getDescent();
                g2.drawString(getText(), textX, textY);
            }

            // Draw the border
            g2.setClip(null);
            g2.setColor(Color.BLACK); // Border color
            g2.setStroke(new BasicStroke(2)); // Border thickness
            g2.draw(circle);

            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(DIAMETER, DIAMETER);
        }
    }
}
