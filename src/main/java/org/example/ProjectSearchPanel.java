package org.example;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectSearchPanel extends JPanel {
    private JTextField searchField;
    private List<Project> originalProjects;
    private Runnable populateProjectsCallback;

    public ProjectSearchPanel(List<Project> projects, Runnable populateProjectsCallback) {
        this.originalProjects = projects;
        this.populateProjectsCallback = populateProjectsCallback;

        setPreferredSize(new Dimension(getWidth(), 60)); // Fixed height of 60 pixels
        setBackground(Color.GRAY); // Replace with SECONDARY_COLOR
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Replace with MARGIN_SIZE

        searchField = new JTextField();
        searchField.setBackground(Color.WHITE); // Replace with PRIMARY_COLOR
        searchField.setPreferredSize(new Dimension(180, 35)); // Set preferred size for text field
        searchField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Add padding (top, left, bottom, right)

        searchField.setForeground(Color.BLACK); // Replace with PRIMARY_TEXT_COLOR
        searchField.setText("Search Project");

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Search Project")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK); // Replace with PRIMARY_TEXT_COLOR
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.BLACK); // Replace with PRIMARY_TEXT_COLOR
                    searchField.setText("Search Project");
                }
            }
        });

        searchField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update(DocumentEvent e) {
                onSearchTextChanged();
            }
        });

        add(searchField, BorderLayout.WEST);
    }

    private void onSearchTextChanged() {
        String searchText = searchField.getText().trim().toLowerCase();

        List<Project> filteredProjects;

        if (searchText.isEmpty()) {
            filteredProjects = originalProjects;
        } else {
            filteredProjects = originalProjects.stream()
                    .filter(project -> project.getName().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
        }

        // Update the projects list and re-populate the UI
        updateProjectList(filteredProjects);
    }

    private void updateProjectList(List<Project> filteredProjects) {
        originalProjects.clear();
        originalProjects.addAll(filteredProjects);
        populateProjectsCallback.run(); // Call the method to populate projects
    }

    // Custom SimpleDocumentListener Interface
    @FunctionalInterface
    interface SimpleDocumentListener extends DocumentListener {
        void update(DocumentEvent e);

        @Override
        default void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        default void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        default void changedUpdate(DocumentEvent e) {
            update(e);
        }
    }
}
