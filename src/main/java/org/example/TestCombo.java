package org.example;//package org.example;
//
//import javax.swing.*;
//import java.awt.*;
//
//public class TestCombo extends JPanel {
//
//    // Define your colors
//    public static final Color SUCCESS_COLOR = new Color(17, 235, 130); // Completed
//    public static final Color INFO_COLOR = new Color(122, 142, 248); // InProgress
//    public static final Color WARNING_COLOR = new Color(244, 162, 97); // Review
//    public static final Color ERROR_COLOR = new Color(251, 93, 93); // ToDo
//    public static final Color SECONDARY_COLOR = new Color(18, 32, 47); // Background color
//
//    public TestCombo() {
//        // Create the combo box with the status values
//        String[] statusOptions = {"ToDo", "InProgress", "Review", "Completed"};
//        JComboBox<String> statusComboBox = new JComboBox<>(statusOptions);
//
//        // Set the custom renderer
//        statusComboBox.setRenderer(new CustomComboBoxRenderer());
//
//        // Set the dropdown background color
//        statusComboBox.setBackground(SECONDARY_COLOR);
//
//        // Attach the combo box to a label with the specified background color
//        JLabel label = new JLabel("Status:");
//        label.setOpaque(true);
//        label.setBackground(SECONDARY_COLOR);
//        label.setForeground(Color.WHITE);
//
//        setLayout(new BorderLayout());
//        add(label, BorderLayout.WEST);
//        add(statusComboBox, BorderLayout.CENTER);
//    }
//
//    private class CustomComboBoxRenderer extends DefaultListCellRenderer {
//        @Override
//        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//
//            // Set text color based on the value
//            if (value != null) {
//                switch (value.toString()) {
//                    case "ToDo":
//                        setForeground(ERROR_COLOR);
//                        break;
//                    case "InProgress":
//                        setForeground(INFO_COLOR);
//                        break;
//                    case "Review":
//                        setForeground(WARNING_COLOR);
//                        break;
//                    case "Completed":
//                        setForeground(SUCCESS_COLOR);
//                        break;
//                    default:
//                        setForeground(Color.WHITE);
//                        break;
//                }
//            }
//
//            // Set the background color of the dropdown
//            if (isSelected) {
//                c.setBackground(SECONDARY_COLOR.darker());
//            } else {
//                c.setBackground(SECONDARY_COLOR);
//            }
//
//            return c;
//        }
//    }
//
//    public static void main(String[] args) {
//        // Create the frame
//        JFrame frame = new JFrame("Custom Status ComboBox");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(300, 100);
//        frame.setLayout(new BorderLayout());
//
//        // Add the custom combo box
//        CustomStatusComboBox customComboBox = new CustomStatusComboBox();
//        frame.add(customComboBox, BorderLayout.CENTER);
//
//        // Display the frame
//        frame.setVisible(true);
//    }
//}
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TestCombo {

    public static final Color SECONDARY_COLOR = new Color(0, 36, 51); // Color of components

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Status Dropdown Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new FlowLayout());

            // Create the status options
            String[] statuses = {"Completed", "Review", "ToDo", "InProgress"};
            JComboBox<String> statusDropdown = new JComboBox<>(statuses);

            // Set dimensions and background color
            statusDropdown.setPreferredSize(new Dimension(180, 25));
            statusDropdown.setBackground(SECONDARY_COLOR);
            statusDropdown.setForeground(Color.WHITE); // Optionally set text color

            // Add ActionListener to handle selection changes
            statusDropdown.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String selectedStatus = (String) statusDropdown.getSelectedItem();
                    filter(selectedStatus); // Call the filter method with the selected status
                }
            });

            // Add the JComboBox to the frame
            frame.add(statusDropdown);
            frame.pack();
            frame.setVisible(true);
        });
    }

    // Sample filter method
    public static void filter(String status) {
        // Print the selected status to the terminal
        System.out.println("Selected status: " + status);
    }
}
