// CrowdfundingPlatform.java
// This is the "brain" of the application.
// It stores all users, campaigns and contributions in ArrayList / HashMap,
// and provides simple methods to add and find them.
// We are NOT using a database here - everything is kept in memory while
// the program is running. This keeps the project at a beginner level.

import java.util.ArrayList;
import java.util.HashMap;

public class CrowdfundingPlatform {

    // The three main collections of data
    private ArrayList<User> users;
    private ArrayList<Campaign> campaigns;
    private ArrayList<Contribution> contributions;

    // System-wide settings (key -> value)
    private HashMap<String, String> settings;

    // A simple log of everything that happens (for the admin to watch)
    private ArrayList<String> activityLog;

    // Counters used to give every new object a unique id
    private int nextUserId;
    private int nextCampaignId;
    private int nextContributionId;

    // Constructor: sets up empty collections and a default admin account
    public CrowdfundingPlatform() {
        users = new ArrayList<User>();
        campaigns = new ArrayList<Campaign>();
        contributions = new ArrayList<Contribution>();
        settings = new HashMap<String, String>();
        activityLog = new ArrayList<String>();

        nextUserId = 1;
        nextCampaignId = 1;
        nextContributionId = 1;

        // A ready-made admin account so we can log in straight away
        addUser("Platform Admin", "admin@crowd.com", "admin123", "Admin");

        // Some default system settings
        settings.put("Platform Name", "CrowdFund");
        settings.put("Currency", "INR");
        settings.put("Allow New Campaigns", "yes");
    }

    // ------------------- Activity log -------------------

    public void log(String message) {
        activityLog.add(message);
    }

    public ArrayList<String> getActivityLog() {
        return activityLog;
    }

    // ------------------- Users -------------------

    // Create a new user and return it
    public User addUser(String name, String email, String password, String role) {
        User u = new User(nextUserId, name, email, password, role);
        nextUserId++;
        users.add(u);
        return u;
    }

    // Find a user by email. Returns null if nobody has that email.
    public User findUserByEmail(String email) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getEmail().equalsIgnoreCase(email)) {
                return users.get(i);
            }
        }
        return null;
    }

    // Delete a user by email. Returns true if a user was removed.
    public boolean deleteUser(String email) {
        User u = findUserByEmail(email);
        if (u == null) {
            return false;
        }
        users.remove(u);
        return true;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    // ------------------- Campaigns -------------------

    // Create a new campaign (it starts as PENDING)
    public Campaign createCampaign(String title, String description, double goalAmount, String creatorEmail) {
        Campaign c = new Campaign(nextCampaignId, title, description, goalAmount, creatorEmail);
        nextCampaignId++;
        campaigns.add(c);
        return c;
    }

    // Find a campaign by its id. Returns null if not found.
    public Campaign findCampaignById(int id) {
        for (int i = 0; i < campaigns.size(); i++) {
            if (campaigns.get(i).getId() == id) {
                return campaigns.get(i);
            }
        }
        return null;
    }

    public ArrayList<Campaign> getCampaigns() {
        return campaigns;
    }

    // All campaigns made by one creator
    public ArrayList<Campaign> getCampaignsByCreator(String email) {
        ArrayList<Campaign> result = new ArrayList<Campaign>();
        for (int i = 0; i < campaigns.size(); i++) {
            if (campaigns.get(i).getCreatorEmail().equalsIgnoreCase(email)) {
                result.add(campaigns.get(i));
            }
        }
        return result;
    }

    // Only the campaigns that the admin has approved (open for contribution)
    public ArrayList<Campaign> getApprovedCampaigns() {
        ArrayList<Campaign> result = new ArrayList<Campaign>();
        for (int i = 0; i < campaigns.size(); i++) {
            if (campaigns.get(i).getStatus().equals("APPROVED")) {
                result.add(campaigns.get(i));
            }
        }
        return result;
    }

    // ------------------- Contributions -------------------

    // Add a contribution to a campaign and increase the campaign's raised amount
    public Contribution addContribution(Campaign c, String contributorEmail, double amount) {
        Contribution con = new Contribution(nextContributionId, c.getId(), c.getTitle(),
                contributorEmail, amount, java.time.LocalDate.now().toString());
        nextContributionId++;
        contributions.add(con);
        c.addMoney(amount);
        return con;
    }

    // All contributions made by one contributor
    public ArrayList<Contribution> getContributionsByUser(String email) {
        ArrayList<Contribution> result = new ArrayList<Contribution>();
        for (int i = 0; i < contributions.size(); i++) {
            if (contributions.get(i).getContributorEmail().equalsIgnoreCase(email)) {
                result.add(contributions.get(i));
            }
        }
        return result;
    }

    // All contributions received by one campaign
    public ArrayList<Contribution> getContributionsByCampaign(int campaignId) {
        ArrayList<Contribution> result = new ArrayList<Contribution>();
        for (int i = 0; i < contributions.size(); i++) {
            if (contributions.get(i).getCampaignId() == campaignId) {
                result.add(contributions.get(i));
            }
        }
        return result;
    }

    // ------------------- Settings -------------------

    public HashMap<String, String> getSettings() {
        return settings;
    }

    public void updateSetting(String key, String value) {
        settings.put(key, value);
    }
}
