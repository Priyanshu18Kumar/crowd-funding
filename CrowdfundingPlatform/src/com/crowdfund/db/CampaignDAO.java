package com.crowdfund.db;

import com.crowdfund.exception.DataAccessException;
import com.crowdfund.model.Campaign;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** All database operations for campaigns (and their update messages). */
public class CampaignDAO implements DAO<Campaign> {

    @Override
    public void insert(Campaign campaign) throws DataAccessException {
        String sql = "INSERT INTO campaigns (title, description, goal_amount, raised_amount, creator_email, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, campaign.getTitle());
            statement.setString(2, campaign.getDescription());
            statement.setDouble(3, campaign.getGoalAmount());
            statement.setDouble(4, campaign.getRaisedAmount());
            statement.setString(5, campaign.getCreatorEmail());
            statement.setString(6, campaign.getStatus());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Could not create campaign: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Campaign campaign) throws DataAccessException {
        String sql = "UPDATE campaigns SET title = ?, description = ?, goal_amount = ?, "
                + "raised_amount = ?, creator_email = ?, status = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, campaign.getTitle());
            statement.setString(2, campaign.getDescription());
            statement.setDouble(3, campaign.getGoalAmount());
            statement.setDouble(4, campaign.getRaisedAmount());
            statement.setString(5, campaign.getCreatorEmail());
            statement.setString(6, campaign.getStatus());
            statement.setInt(7, campaign.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Could not update campaign: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws DataAccessException {
        String sql = "DELETE FROM campaigns WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Could not delete campaign: " + e.getMessage(), e);
        }
    }

    @Override
    public Campaign findById(int id) throws DataAccessException {
        String sql = "SELECT * FROM campaigns WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return map(result);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not read campaign: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Campaign> findAll() throws DataAccessException {
        return runList("SELECT * FROM campaigns ORDER BY id", null);
    }

    /** Campaigns made by one creator. */
    public List<Campaign> findByCreator(String creatorEmail) throws DataAccessException {
        return runList("SELECT * FROM campaigns WHERE creator_email = ? ORDER BY id", creatorEmail);
    }

    /** Campaigns with a given status, e.g. only "APPROVED" ones. */
    public List<Campaign> findByStatus(String status) throws DataAccessException {
        return runList("SELECT * FROM campaigns WHERE status = ? ORDER BY id", status);
    }

    /** Increase a campaign's raised amount (called when someone contributes). */
    public void addRaisedAmount(int campaignId, double amount) throws DataAccessException {
        String sql = "UPDATE campaigns SET raised_amount = raised_amount + ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, amount);
            statement.setInt(2, campaignId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Could not update campaign total: " + e.getMessage(), e);
        }
    }

    /** Post a progress update message for a campaign. */
    public void addUpdate(int campaignId, String message) throws DataAccessException {
        String sql = "INSERT INTO campaign_updates (campaign_id, message) VALUES (?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, campaignId);
            statement.setString(2, message);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Could not post update: " + e.getMessage(), e);
        }
    }

    /** All update messages for a campaign. */
    public List<String> findUpdates(int campaignId) throws DataAccessException {
        String sql = "SELECT message FROM campaign_updates WHERE campaign_id = ? ORDER BY id";
        List<String> updates = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, campaignId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    updates.add(result.getString("message"));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not read updates: " + e.getMessage(), e);
        }
        return updates;
    }

    // ---- helpers ----

    private List<Campaign> runList(String sql, String parameter) throws DataAccessException {
        List<Campaign> campaigns = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (parameter != null) {
                statement.setString(1, parameter);
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    campaigns.add(map(result));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not read campaigns: " + e.getMessage(), e);
        }
        return campaigns;
    }

    private Campaign map(ResultSet result) throws SQLException {
        return new Campaign(
                result.getInt("id"),
                result.getString("title"),
                result.getString("description"),
                result.getDouble("goal_amount"),
                result.getDouble("raised_amount"),
                result.getString("creator_email"),
                result.getString("status"));
    }
}
