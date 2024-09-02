package org.example;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class CustomStatusComboBox extends JComboBox<String> {
    public static final Color SECONDARY_COLOR = new Color(0, 36, 51);
    public static final Color PRIMARY_COLOR = new Color(3, 50, 70);
    public static final Color PRIMARY_TEXT_COLOR = new Color(255, 255, 255);
    public static final Color SECONDARY_TEXT_COLOR = new Color(160, 172, 187);
    /* Status colors */
    public static final Color SUCCESS_COLOR = new Color(17, 235, 130);//Completed
    public static final Color INFO_COLOR = new Color(122, 142, 248);//Inprogress
    public static final Color WARNING_COLOR = new Color(244, 162, 97);//Review
    public static final Color ERROR_COLOR = new Color(251, 93, 93);//Todo

    public CustomStatusComboBox() {
        setModel(new DefaultComboBoxModel<>(new String[]{"Completed", "ToDo", "InProgress", "Review"}));
        setRenderer(new StatusRenderer());
        setUI(new CustomComboBoxUI());
        setBackground(PRIMARY_COLOR);
        setBorder(new EmptyBorder(5, 5, 5, 5)); // Add padding to the text
// Inside CustomStatusComboBox constructor
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
//                setBackground();
                setForeground(Color.WHITE); // Reset text color
            }

            @Override
            public void focusLost(FocusEvent e) {
                setForeground(Color.WHITE); // Reset text color
            }
        });

    }


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

    private static class StatusRenderer extends JLabel implements ListCellRenderer<String> {
        public StatusRenderer() {
            setOpaque(true);
            setFont(new Font("Arial", Font.BOLD, 14)); // Adjust font style and size
            setBorder(new EmptyBorder(5, 5, 5, 5)); // Add padding to the text
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value);

            switch (value) {
                case "Completed":
                    setBackground(SUCCESS_COLOR); // Green
                    setForeground(Color.WHITE);
                    break;
                case "ToDo":
                    setBackground(ERROR_COLOR); // Red
                    setForeground(Color.WHITE);
                    break;
                case "InProgress":
                    setBackground(INFO_COLOR); // Blue
                    setForeground(Color.WHITE);
                    break;
                case "Review":
                    setBackground(WARNING_COLOR); // Orange
                    setForeground(Color.WHITE);
                    break;
                default:
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                    break;
            }

            if (isSelected) {
                setBackground(getBackground().darker());
            }

            return this;
        }
    }

    private static class CustomComboBoxUI extends BasicComboBoxUI {
//        @Override
//        protected JButton createArrowButton() {
//            JButton arrowButton = new JButton("▼");
//            arrowButton.setBorder(BorderFactory.createEmptyBorder());
//            arrowButton.setContentAreaFilled(false);
//            arrowButton.setForeground(Color.GREEN); // Set arrow color
//            return arrowButton;
//        }

        @Override
        protected JButton createArrowButton() {
            // Load the icon from the specified path, adjust the dimensions as needed
            ImageIcon arrowIcon = loadImageIcon("icons/status-drop-down-arrow.png", 20, 20);

            // Create a new JButton for the arrow button
            JButton arrowButton = new JButton();

            // Set the icon to the button if it's successfully loaded
            if (arrowIcon != null) {
                arrowButton.setIcon(arrowIcon);
            } else {
                arrowButton.setText("▼"); // Fallback to text if icon fails to load
            }

            // Set additional properties for the arrow button
            arrowButton.setBorder(BorderFactory.createEmptyBorder());
            arrowButton.setContentAreaFilled(false);
            arrowButton.setForeground(Color.GREEN); // Set arrow color

            return arrowButton;
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = (BasicComboPopup) super.createPopup();
            popup.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // Set border color
            popup.setBackground(new Color(18, 32, 47)); // Dark background
            return popup;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Custom JComboBox");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 300);
        frame.setLayout(new FlowLayout());

        CustomStatusComboBox comboBox = new CustomStatusComboBox();
        frame.add(comboBox);

        frame.setVisible(true);
    }
}
