package com.crowdfund.model;

/** A single contribution made to a campaign. */
public class Contribution {

    private int id;
    private int campaignId;
    private String campaignTitle;   // kept for easy display in tables
    private String contributorEmail;
    private double amount;
    private String date;

    public Contribution(int id, int campaignId, String campaignTitle,
                        String contributorEmail, double amount, String date) {
        this.id = id;
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.contributorEmail = contributorEmail;
        this.amount = amount;
        this.date = date;
    }

    public int getId() { return id; }
    public int getCampaignId() { return campaignId; }
    public String getCampaignTitle() { return campaignTitle; }
    public String getContributorEmail() { return contributorEmail; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }

    public void setCampaignTitle(String campaignTitle) { this.campaignTitle = campaignTitle; }

    @Override
    public String toString() {
        return campaignTitle + " : " + amount + " on " + date;
    }
}
