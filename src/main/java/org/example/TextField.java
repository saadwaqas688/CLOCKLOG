package org.example;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class TextField extends JTextField {
    private static final String PLACEHOLDER_TEXT = "Search Project";
    private static final Color PRIMARY_TEXT_COLOR = Color.BLACK; // Define this color as per your theme
    private static final int LEFT_MARGIN = 10; // Margin from left side
    private static final int VERTICAL_MARGIN = 5; // Vertical margins (top and bottom)
    private static final int BORDER_RADIUS = 10; // Radius for rounded corners

    public TextField() {
        // Set initial placeholder text
        setText(PLACEHOLDER_TEXT);
        setForeground(Color.GRAY);
        setBackground(Color.WHITE); // Set background color

        // Add custom rounded border with padding
        setBorder(new RoundedBorder(BORDER_RADIUS, VERTICAL_MARGIN, LEFT_MARGIN, 10));

        setPreferredSize(new Dimension(180, 35));

        // Add focus listener to handle placeholder behavior
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(PLACEHOLDER_TEXT)) {
                    setText("");
                    setForeground(PRIMARY_TEXT_COLOR); // Set text color when user types
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    setText(PLACEHOLDER_TEXT);
                    setForeground(Color.GRAY); // Set placeholder color
                }
            }
        });
    }

    // Custom border with rounded corners
    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final int topMargin;
        private final int leftMargin;
        private final int rightMargin;

        RoundedBorder(int radius, int topMargin, int leftMargin, int rightMargin) {
            this.radius = radius;
            this.topMargin = topMargin;
            this.leftMargin = leftMargin;
            this.rightMargin = rightMargin;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.GRAY);

            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);

            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(topMargin, leftMargin, topMargin, rightMargin);
        }
    }

    // Main method to test the TextField class
    public static void main(String[] args) {
        JFrame frame = new JFrame("TextField Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 100);
        frame.setLayout(new FlowLayout());

        TextField textField = new TextField();
        frame.add(textField);

        frame.setVisible(true);
    }
}
