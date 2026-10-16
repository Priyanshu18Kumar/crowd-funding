package com.crowdfund.ui;

import com.crowdfund.exception.ValidationException;

import javax.swing.JOptionPane;
import java.awt.Component;

/** Small helpers shared by the screens (dialogs and input parsing). */
public class UiUtil {

    private UiUtil() {
        // utility class
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Done", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Please check", JOptionPane.WARNING_MESSAGE);
    }

    /** Read a positive number from a text field, or throw a ValidationException. */
    public static double parseAmount(String text, String fieldName) throws ValidationException {
        double value;
        try {
            value = Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a number.");
        }
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
        return value;
    }

    /** Read a whole number from a text field, or throw a ValidationException. */
    public static int parseId(String text, String fieldName) throws ValidationException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a whole number.");
        }
    }
}
