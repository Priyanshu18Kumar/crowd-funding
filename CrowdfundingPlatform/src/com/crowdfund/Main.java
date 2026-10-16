package com.crowdfund;

import com.crowdfund.db.DBConnection;
import com.crowdfund.exception.DataAccessException;
import com.crowdfund.service.ActivityLogger;
import com.crowdfund.ui.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Program entry point. Starts the background logger, checks the database
 * connection, then opens the login window.
 */
public class Main {

    public static void main(String[] args) {
        // Start the background thread that writes the activity log.
        ActivityLogger.getInstance().start();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    DBConnection.testConnection();
                } catch (DataAccessException e) {
                    JOptionPane.showMessageDialog(null,
                            e.getMessage() + "\n\nThe window will still open, but database actions will fail.",
                            "Database warning", JOptionPane.WARNING_MESSAGE);
                }
                new LoginFrame().setVisible(true);
            }
        });
    }
}
