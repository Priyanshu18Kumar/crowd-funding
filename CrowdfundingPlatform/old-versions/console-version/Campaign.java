// Campaign.java
// This class represents one fundraising campaign.
// It also keeps a list of "updates" that the creator posts for contributors.

import java.util.ArrayList;

public class Campaign {

    private int id;                    // unique id of the campaign
    private String title;              // campaign title
    private String description;        // what the campaign is about
    private double goalAmount;         // how much money is needed
    private double raisedAmount;       // how much money is collected so far
    private String creatorEmail;       // email of the creator who made it
    private String status;             // "PENDING", "APPROVED" or "REJECTED"
    private ArrayList<String> updates; // list of progress update messages

    // Constructor
    public Campaign(int id, String title, String description, double goalAmount, String creatorEmail) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.goalAmount = goalAmount;
        this.raisedAmount = 0.0;       // a new campaign starts with 0 collected
        this.creatorEmail = creatorEmail;
        this.status = "PENDING";       // a new campaign waits for admin approval
        this.updates = new ArrayList<String>();
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getGoalAmount() { return goalAmount; }
    public double getRaisedAmount() { return raisedAmount; }
    public String getCreatorEmail() { return creatorEmail; }
    public String getStatus() { return status; }
    public ArrayList<String> getUpdates() { return updates; }

    // Change the approval status (used by the admin)
    public void setStatus(String status) {
        this.status = status;
    }

    // Add money to this campaign when someone contributes
    public void addMoney(double amount) {
        this.raisedAmount = this.raisedAmount + amount;
    }

    // Add a progress update message (used by the creator)
    public void addUpdate(String message) {
        updates.add(message);
    }

    // How far along the campaign is, as a percentage (never more than 100)
    public double getProgress() {
        if (goalAmount == 0) {
            return 0;
        }
        double percent = (raisedAmount / goalAmount) * 100;
        if (percent > 100) {
            percent = 100;
        }
        return percent;
    }

    // How a Campaign looks when printed
    public String toString() {
        return id + " | " + title + " | Goal: " + goalAmount
                + " | Raised: " + raisedAmount + " | " + status;
    }
}
