package com.crowdfund.db;

import com.crowdfund.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Database operations for the activity log. */
public class ActivityLogDAO {

    public void insert(String message) throws DataAccessException {
        String sql = "INSERT INTO activity_log (message) VALUES (?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, message);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Could not write activity log: " + e.getMessage(), e);
        }
    }

    public List<String> findAll() throws DataAccessException {
        String sql = "SELECT message FROM activity_log ORDER BY id DESC LIMIT 100";
        List<String> messages = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                messages.add(result.getString("message"));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not read activity log: " + e.getMessage(), e);
        }
        return messages;
    }
}
