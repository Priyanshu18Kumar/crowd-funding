package com.crowdfund.ui;

import com.crowdfund.db.CampaignDAO;
import com.crowdfund.db.ContributionDAO;
import com.crowdfund.exception.DataAccessException;
import com.crowdfund.exception.ValidationException;
import com.crowdfund.model.Campaign;
import com.crowdfund.model.Contribution;
import com.crowdfund.model.Creator;
import com.crowdfund.service.ActivityLogger;
import com.crowdfund.service.CampaignMonitor;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/** The campaign creator dashboard. */
public class CreatorFrame extends JFrame implements CampaignMonitor.Listener {

    private final Creator creator;
    private final CampaignDAO campaignDAO = new CampaignDAO();
    private final ContributionDAO contributionDAO = new ContributionDAO();
    private final CampaignMonitor monitor = new CampaignMonitor(this);

    private final JTextField titleField = new JTextField(22);
    private final JTextField descField = new JTextField(22);
    private final JTextField goalField = new JTextField(12);

    private final DefaultTableModel campaignModel =
            new DefaultTableModel(new Object[]{"ID", "Title", "Goal", "Raised", "Progress", "Status"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
    private final JTable campaignTable = new JTable(campaignModel);

    private final JTextField updateIdField = new JTextField(6);
    private final JTextField updateMsgField = new JTextField(22);

    private final JTextArea analyticsArea = new JTextArea();
    private final JLabel statusBar = new JLabel(" ");

    public CreatorFrame(Creator creator) {
        super(creator.getDashboardTitle());
        this.creator = creator;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 580);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Create Campaign", buildCreateTab());
        tabs.addTab("My Campaigns", buildMyCampaignsTab());
        tabs.addTab("Post Update", buildUpdateTab());
        tabs.addTab("Analytics", buildAnalyticsTab());

        JLabel welcome = new JLabel("Signed in as " + creator.getName() + " (Creator)");
        welcome.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 10, 6, 10));

        add(welcome, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        refreshMyCampaigns();
        refreshAnalytics();
        monitor.start();
    }

    // ---- Create campaign ----

    private JPanel buildCreateTab() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Title"), gbc);
        gbc.gridx = 1; form.add(titleField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Description"), gbc);
        gbc.gridx = 1; form.add(descField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Goal amount"), gbc);
        gbc.gridx = 1; form.add(goalField, gbc);

        JButton create = new JButton("Create Campaign");
        create.addActionListener(e -> createCampaign());
        gbc.gridx = 1; gbc.gridy = 3; form.add(create, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    private void createCampaign() {
        try {
            String title = titleField.getText().trim();
            String description = descField.getText().trim();
            if (title.isEmpty()) {
                throw new ValidationException("Title is required.");
            }
            double goal = UiUtil.parseAmount(goalField.getText(), "Goal amount");

            Campaign campaign = new Campaign(title, description, goal, creator.getEmail());
            campaignDAO.insert(campaign);
            ActivityLogger.getInstance().log("Campaign created: " + title + " by " + creator.getEmail());
            UiUtil.info(this, "Campaign created. It is now waiting for admin approval.");
            titleField.setText("");
            descField.setText("");
            goalField.setText("");
            refreshMyCampaigns();
            refreshAnalytics();
        } catch (ValidationException e) {
            UiUtil.warn(this, e.getMessage());
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    // ---- My campaigns ----

    private JPanel buildMyCampaignsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(campaignTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh");
        buttons.add(refresh);
        panel.add(buttons, BorderLayout.SOUTH);
        refresh.addActionListener(e -> refreshMyCampaigns());
        return panel;
    }

    private void refreshMyCampaigns() {
        try {
            campaignModel.setRowCount(0);
            for (Campaign c : campaignDAO.findByCreator(creator.getEmail())) {
                campaignModel.addRow(new Object[]{
                        c.getId(),
                        c.getTitle(),
                        String.format("%.0f", c.getGoalAmount()),
                        String.format("%.0f", c.getRaisedAmount()),
                        String.format("%.1f%%", c.getProgress()),
                        c.getStatus()});
            }
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    // ---- Post update ----

    private JPanel buildUpdateTab() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Campaign ID"));
        form.add(updateIdField);
        form.add(new JLabel("Message"));
        form.add(updateMsgField);
        JButton post = new JButton("Post update");
        post.addActionListener(e -> postUpdate());
        form.add(post);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    private void postUpdate() {
        try {
            int id = UiUtil.parseId(updateIdField.getText(), "Campaign ID");
            String message = updateMsgField.getText().trim();
            if (message.isEmpty()) {
                throw new ValidationException("Message is required.");
            }

            Campaign campaign = campaignDAO.findById(id);
            if (campaign == null || !campaign.getCreatorEmail().equalsIgnoreCase(creator.getEmail())) {
                throw new ValidationException("Campaign not found or not yours.");
            }
            campaignDAO.addUpdate(id, message);
            ActivityLogger.getInstance().log("Progress update posted on: " + campaign.getTitle());
            UiUtil.info(this, "Update posted.");
            updateMsgField.setText("");
            refreshAnalytics();
        } catch (ValidationException e) {
            UiUtil.warn(this, e.getMessage());
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    // ---- Analytics ----

    private JPanel buildAnalyticsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        analyticsArea.setEditable(false);
        panel.add(new JScrollPane(analyticsArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh");
        buttons.add(refresh);
        panel.add(buttons, BorderLayout.SOUTH);
        refresh.addActionListener(e -> refreshAnalytics());
        return panel;
    }

    private void refreshAnalytics() {
        try {
            StringBuilder sb = new StringBuilder();
            List<Campaign> campaigns = campaignDAO.findByCreator(creator.getEmail());
            if (campaigns.isEmpty()) {
                sb.append("No campaigns yet.\n");
            }
            for (Campaign c : campaigns) {
                List<Contribution> contributions = contributionDAO.findByCampaign(c.getId());
                double total = 0;
                for (Contribution con : contributions) {
                    total += con.getAmount();
                }
                sb.append("Campaign: ").append(c.getTitle())
                        .append(" (").append(c.getStatus()).append(")\n");
                sb.append("   Contributors: ").append(contributions.size()).append("\n");
                sb.append("   Raised: ").append(String.format("%.0f", total))
                        .append(" of ").append(String.format("%.0f", c.getGoalAmount())).append("\n");
                sb.append("   Progress: ").append(String.format("%.1f%%", c.getProgress())).append("\n");
                for (String update : campaignDAO.findUpdates(c.getId())) {
                    sb.append("     - ").append(update).append("\n");
                }
                sb.append("\n");
            }
            analyticsArea.setText(sb.toString());
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    @Override
    public void onTotals(double totalRaised, double totalGoal, int campaignCount) {
        statusBar.setText(String.format("Live: %d campaigns platform-wide  |  raised %.0f of %.0f",
                campaignCount, totalRaised, totalGoal));
    }
}
