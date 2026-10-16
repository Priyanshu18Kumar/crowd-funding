package com.crowdfund.db;

import com.crowdfund.exception.DataAccessException;
import com.crowdfund.model.User;
import com.crowdfund.service.UserFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** All database operations for users. */
public class UserDAO implements DAO<User> {

    @Override
    public void insert(User user) throws DataAccessException {
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getRole());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Could not add user: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(User user) throws DataAccessException {
        String sql = "UPDATE users SET name = ?, email = ?, password = ?, role = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getRole());
            statement.setInt(5, user.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Could not update user: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws DataAccessException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Could not delete user: " + e.getMessage(), e);
        }
    }

    @Override
    public User findById(int id) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return map(result);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not read user: " + e.getMessage(), e);
        }
        return null;
    }

    /** Used by the login screen. */
    public User findByEmail(String email) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return map(result);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not read user: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<User> findAll() throws DataAccessException {
        String sql = "SELECT * FROM users ORDER BY id";
        List<User> users = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                users.add(map(result));
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not read users: " + e.getMessage(), e);
        }
        return users;
    }

    private User map(ResultSet result) throws SQLException {
        return UserFactory.create(
                result.getString("role"),
                result.getInt("id"),
                result.getString("name"),
                result.getString("email"),
                result.getString("password"));
    }
}
