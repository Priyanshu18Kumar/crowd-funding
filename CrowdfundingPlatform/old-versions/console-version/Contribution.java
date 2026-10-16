// Contribution.java
// This class represents one contribution (one payment) made to a campaign.

public class Contribution {

    private int id;                     // unique id of this contribution
    private int campaignId;             // which campaign was supported
    private String campaignTitle;       // campaign title (kept for easy display)
    private String contributorEmail;    // who contributed
    private double amount;              // how much money was given
    private String date;                // the date of the contribution

    // Constructor
    public Contribution(int id, int campaignId, String campaignTitle,
                        String contributorEmail, double amount, String date) {
        this.id = id;
        this.campaignId = campaignId;
        this.campaignTitle = campaignTitle;
        this.contributorEmail = contributorEmail;
        this.amount = amount;
        this.date = date;
    }

    // Getters
    public int getId() { return id; }
    public int getCampaignId() { return campaignId; }
    public String getCampaignTitle() { return campaignTitle; }
    public String getContributorEmail() { return contributorEmail; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }

    // How a Contribution looks when printed
    public String toString() {
        return id + " | " + campaignTitle + " | " + amount + " | " + date;
    }
}
