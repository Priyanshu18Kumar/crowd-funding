package com.crowdfund.db;

import com.crowdfund.exception.DataAccessException;
import com.crowdfund.model.Contribution;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** All database operations for contributions. */
public class ContributionDAO implements DAO<Contribution> {

    // We join with the campaigns table so each contribution also carries the
    // campaign title, which is handy for display.
    private static final String SELECT_JOIN =
            "SELECT con.id, con.campaign_id, c.title AS campaign_title, "
                    + "con.contributor_email, con.amount, con.contrib_date "
                    + "FROM contributions con JOIN campaigns c ON con.campaign_id = c.id ";

    @Override
    public void insert(Contribution contribution) throws DataAccessException {
        String sql = "INSERT INTO contributions (campaign_id, contributor_email, amount, contrib_date) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, contribution.getCampaignId());
            statement.setString(2, contribution.getContributorEmail());
            statement.setDouble(3, contribution.getAmount());
            statement.setString(4, contribution.getDate());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Could not record contribution: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Contribution contribution) throws DataAccessException {
        String sql = "UPDATE contributions SET amount = ?, contrib_date = ? WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, contribution.getAmount());
            statement.setString(2, contribution.getDate());
            statement.setInt(3, contribution.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Could not update contribution: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws DataAccessException {
        String sql = "DELETE FROM contributions WHERE id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Could not delete contribution: " + e.getMessage(), e);
        }
    }

    @Override
    public Contribution findById(int id) throws DataAccessException {
        String sql = SELECT_JOIN + "WHERE con.id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return map(result);
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not read contribution: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<Contribution> findAll() throws DataAccessException {
        return runList(SELECT_JOIN + "ORDER BY con.id", null, 0);
    }

    /** Contributions made by one contributor. */
    public List<Contribution> findByContributor(String email) throws DataAccessException {
        return runList(SELECT_JOIN + "WHERE con.contributor_email = ? ORDER BY con.id", email, 0);
    }

    /** Contributions received by one campaign. */
    public List<Contribution> findByCampaign(int campaignId) throws DataAccessException {
        return runList(SELECT_JOIN + "WHERE con.campaign_id = ? ORDER BY con.id", null, campaignId);
    }

    /** Total amount contributed to one campaign. */
    public double totalForCampaign(int campaignId) throws DataAccessException {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM contributions WHERE campaign_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, campaignId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getDouble("total");
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not total contributions: " + e.getMessage(), e);
        }
        return 0;
    }

    // ---- helpers ----

    private List<Contribution> runList(String sql, String email, int campaignId) throws DataAccessException {
        List<Contribution> contributions = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (email != null) {
                statement.setString(1, email);
            } else if (campaignId > 0) {
                statement.setInt(1, campaignId);
            }
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    contributions.add(map(result));
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Could not read contributions: " + e.getMessage(), e);
        }
        return contributions;
    }

    private Contribution map(ResultSet result) throws SQLException {
        return new Contribution(
                result.getInt("id"),
                result.getInt("campaign_id"),
                result.getString("campaign_title"),
                result.getString("contributor_email"),
                result.getDouble("amount"),
                result.getString("contrib_date"));
    }
}
