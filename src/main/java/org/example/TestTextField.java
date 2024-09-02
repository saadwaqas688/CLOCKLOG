package org.example;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TestTextField{

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TestTextField::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("TextField with Button");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        // Create the text field
        JTextField textField = new JTextField(20);
        textField.setText("Click here to focus");
        textField.setForeground(Color.GRAY);
        textField.setFocusable(false); // Prevent auto-focus

        // Create the button
        JButton button = new JButton("Submit");

        // Add action listener for the button
        button.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "Button Clicked");
        });

        // Add components to the frame
        frame.add(textField);
        frame.add(button);

        // Add mouse listener to handle focus
        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!textField.hasFocus()) {
                    textField.setFocusable(true);
                    textField.requestFocus();
                    textField.setForeground(Color.BLACK);
                }
            }
        });

        // Add focus listener to handle losing focus
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Reset text color when focus is lost
                textField.setForeground(Color.GRAY);
                textField.setFocusable(false);
            }
        });

        // Add a mouse listener to the frame to lose focus when clicking outside
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (textField.hasFocus() && !textField.getBounds().contains(e.getPoint())) {
                    textField.transferFocus(); // Moves focus to the next component
                }
            }
        });

        // Set frame size and make it visible
        frame.setSize(300, 100);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
