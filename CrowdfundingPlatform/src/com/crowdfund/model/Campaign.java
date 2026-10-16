package com.crowdfund.model;

/** A fundraising campaign. */
public class Campaign {

    private int id;
    private String title;
    private String description;
    private double goalAmount;
    private double raisedAmount;
    private String creatorEmail;
    private String status; // PENDING, APPROVED or REJECTED

    public Campaign(int id, String title, String description, double goalAmount,
                    double raisedAmount, String creatorEmail, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.goalAmount = goalAmount;
        this.raisedAmount = raisedAmount;
        this.creatorEmail = creatorEmail;
        this.status = status;
    }

    /** Convenience constructor for a brand new campaign (id is set by the database). */
    public Campaign(String title, String description, double goalAmount, String creatorEmail) {
        this(0, title, description, goalAmount, 0.0, creatorEmail, "PENDING");
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getGoalAmount() { return goalAmount; }
    public double getRaisedAmount() { return raisedAmount; }
    public String getCreatorEmail() { return creatorEmail; }
    public String getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setGoalAmount(double goalAmount) { this.goalAmount = goalAmount; }
    public void setRaisedAmount(double raisedAmount) { this.raisedAmount = raisedAmount; }
    public void setStatus(String status) { this.status = status; }

    /** Progress as a percentage (0 to 100). */
    public double getProgress() {
        if (goalAmount <= 0) return 0;
        double percent = (raisedAmount / goalAmount) * 100;
        return percent > 100 ? 100 : percent;
    }

    @Override
    public String toString() {
        return title + " (" + status + ")";
    }
}
