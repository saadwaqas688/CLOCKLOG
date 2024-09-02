package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class SearchTask extends JTextField {
    private String placeholder;
    private Color placeholderColor;
    private Color textColor;
    private ImageIcon searchIcon;
    private boolean hideIconAndPlaceholder;
    private int placeholderMarginLeft = 10; // Margin for the placeholder text from the left side

    public SearchTask(String placeholder, Color placeholderColor, Color textColor, ImageIcon searchIcon) {
        this.placeholder = placeholder;
        this.placeholderColor = placeholderColor;
        this.textColor = textColor;
        this.searchIcon = searchIcon;
        setForeground(textColor); // Set the regular text color
        setCaretColor(textColor); // Set the caret (cursor) color to match the text color
        setFocusable(false);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                hideIconAndPlaceholder = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                hideIconAndPlaceholder = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!hideIconAndPlaceholder) {
            if (searchIcon != null) {
                int iconY = (getHeight() - searchIcon.getIconHeight()) / 2;
                searchIcon.paintIcon(this, g, 5, iconY);
            }

            if (getText().isEmpty() && placeholder != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(placeholderColor); // Placeholder text color
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                int padding = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
                int iconOffset = searchIcon != null ? searchIcon.getIconWidth() + 5 : 5;
                int placeholderX = iconOffset + placeholderMarginLeft; // Apply margin to the left of the placeholder
                g2.drawString(placeholder, placeholderX, padding);
                g2.dispose();
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            setForeground(placeholderColor); // Use placeholder color when disabled
        } else {
            setForeground(textColor); // Use regular text color when enabled
            setCaretColor(textColor); // Ensure caret color matches the text color
        }
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholderColor(Color placeholderColor) {
        this.placeholderColor = placeholderColor;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(180, 35);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(180, 35);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(180, 35);
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        setForeground(textColor); // Update the text color immediately
        setCaretColor(textColor); // Update the caret color to match the text color
    }

    public void setPlaceholderMarginLeft(int margin) {
        this.placeholderMarginLeft = margin;
        repaint();
    }
}
