package com.crowdfund.db;

import com.crowdfund.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Database operations for the system-wide settings. */
public class SettingsDAO {

    /** All settings as a map of key -> value (kept in key order). */
    public Map<String, String> findAll() throws DataAccessException {
        String sql = "SELECT setting_key, setting_value FROM settings ORDER BY setting_key";
        Map<String, String> settings = new LinkedHashMap<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                settings.put(result.getString("setting_key"), result.getString("setting_value"));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not read settings: " + e.getMessage(), e);
        }
        return settings;
    }

    /**
     * Insert a setting, or update it if it already exists.
     * We do it in two plain steps (update, then insert if nothing changed) so
     * the code stays simple and works on any database, not just MySQL.
     */
    public void save(String key, String value) throws DataAccessException {
        String update = "UPDATE settings SET setting_value = ? WHERE setting_key = ?";
        String insert = "INSERT INTO settings (setting_key, setting_value) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection()) {

            int rows;
            try (PreparedStatement statement = connection.prepareStatement(update)) {
                statement.setString(1, value);
                statement.setString(2, key);
                rows = statement.executeUpdate();
            }

            if (rows == 0) {
                try (PreparedStatement statement = connection.prepareStatement(insert)) {
                    statement.setString(1, key);
                    statement.setString(2, value);
                    statement.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not save setting: " + e.getMessage(), e);
        }
    }
}
