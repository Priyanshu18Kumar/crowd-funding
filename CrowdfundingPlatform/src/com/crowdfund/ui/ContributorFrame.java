package com.crowdfund.ui;

import com.crowdfund.db.CampaignDAO;
import com.crowdfund.db.ContributionDAO;
import com.crowdfund.db.UserDAO;
import com.crowdfund.exception.DataAccessException;
import com.crowdfund.exception.ValidationException;
import com.crowdfund.model.Campaign;
import com.crowdfund.model.Contribution;
import com.crowdfund.model.Contributor;
import com.crowdfund.model.User;
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
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

/** The contributor dashboard. */
public class ContributorFrame extends JFrame implements CampaignMonitor.Listener {

    private final Contributor contributor;
    private final CampaignDAO campaignDAO = new CampaignDAO();
    private final ContributionDAO contributionDAO = new ContributionDAO();
    private final UserDAO userDAO = new UserDAO();
    private final CampaignMonitor monitor = new CampaignMonitor(this);

    private final DefaultTableModel approvedModel =
            new DefaultTableModel(new Object[]{"ID", "Title", "Goal", "Raised", "Progress"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
    private final JTable approvedTable = new JTable(approvedModel);

    private final JTextField contributeIdField = new JTextField(6);
    private final JTextField amountField = new JTextField(10);

    private final DefaultTableModel contributionModel =
            new DefaultTableModel(new Object[]{"Campaign", "Amount", "Date"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
    private final JTable contributionTable = new JTable(contributionModel);
    private final JLabel totalLabel = new JLabel("Total contributed: 0");

    private final JTextArea updatesArea = new JTextArea();

    private final JTextField profileNameField = new JTextField(18);
    private final JTextField profileEmailField = new JTextField(18);

    private final JLabel statusBar = new JLabel(" ");

    public ContributorFrame(Contributor contributor) {
        super(contributor.getDashboardTitle());
        this.contributor = contributor;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 580);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Browse Campaigns", buildBrowseTab());
        tabs.addTab("Contribute", buildContributeTab());
        tabs.addTab("My Contributions", buildHistoryTab());
        tabs.addTab("Updates", buildUpdatesTab());
        tabs.addTab("Profile", buildProfileTab());

        JLabel welcome = new JLabel("Signed in as " + contributor.getName() + " (Contributor)");
        welcome.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 10, 6, 10));

        add(welcome, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        profileNameField.setText(contributor.getName());
        profileEmailField.setText(contributor.getEmail());

        refreshApproved();
        refreshHistory();
        refreshUpdates();
        monitor.start();
    }

    // ---- Browse ----

    private JPanel buildBrowseTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(approvedTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh");
        buttons.add(refresh);
        panel.add(buttons, BorderLayout.SOUTH);
        refresh.addActionListener(e -> refreshApproved());
        return panel;
    }

    private void refreshApproved() {
        try {
            approvedModel.setRowCount(0);
            for (Campaign c : campaignDAO.findByStatus("APPROVED")) {
                approvedModel.addRow(new Object[]{
                        c.getId(),
                        c.getTitle(),
                        String.format("%.0f", c.getGoalAmount()),
                        String.format("%.0f", c.getRaisedAmount()),
                        String.format("%.1f%%", c.getProgress())});
            }
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    // ---- Contribute ----

    private JPanel buildContributeTab() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        form.add(new JLabel("Campaign ID"));
        form.add(contributeIdField);
        form.add(new JLabel("Amount"));
        form.add(amountField);
        JButton contribute = new JButton("Contribute");
        contribute.addActionListener(e -> contribute());
        form.add(contribute);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    private void contribute() {
        try {
            int id = UiUtil.parseId(contributeIdField.getText(), "Campaign ID");
            double amount = UiUtil.parseAmount(amountField.getText(), "Amount");

            Campaign campaign = campaignDAO.findById(id);
            if (campaign == null || !"APPROVED".equals(campaign.getStatus())) {
                throw new ValidationException("Campaign not found or not open for contribution.");
            }

            Contribution contribution = new Contribution(0, id, campaign.getTitle(),
                    contributor.getEmail(), amount, LocalDate.now().toString());
            contributionDAO.insert(contribution);
            campaignDAO.addRaisedAmount(id, amount);
            ActivityLogger.getInstance().log("Contribution of " + amount + " to " + campaign.getTitle());

            UiUtil.info(this, "Contribution confirmed. Thank you!");
            amountField.setText("");
            refreshApproved();
            refreshHistory();
        } catch (ValidationException e) {
            UiUtil.warn(this, e.getMessage());
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    // ---- History ----

    private JPanel buildHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(contributionTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshHistory());
        south.add(refresh, BorderLayout.WEST);
        south.add(totalLabel, BorderLayout.EAST);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshHistory() {
        try {
            contributionModel.setRowCount(0);
            double total = 0;
            for (Contribution c : contributionDAO.findByContributor(contributor.getEmail())) {
                total += c.getAmount();
                contributionModel.addRow(new Object[]{
                        c.getCampaignTitle(),
                        String.format("%.0f", c.getAmount()),
                        c.getDate()});
            }
            totalLabel.setText("Total contributed: " + String.format("%.0f", total) + "   ");
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    // ---- Updates ----

    private JPanel buildUpdatesTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        updatesArea.setEditable(false);
        panel.add(new JScrollPane(updatesArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh");
        buttons.add(refresh);
        panel.add(buttons, BorderLayout.SOUTH);
        refresh.addActionListener(e -> refreshUpdates());
        return panel;
    }

    private void refreshUpdates() {
        try {
            StringBuilder sb = new StringBuilder();
            for (Campaign c : campaignDAO.findByStatus("APPROVED")) {
                List<String> updates = campaignDAO.findUpdates(c.getId());
                if (updates.isEmpty()) {
                    continue;
                }
                sb.append(c.getTitle()).append(":\n");
                for (String update : updates) {
                    sb.append("   - ").append(update).append("\n");
                }
                sb.append("\n");
            }
            if (sb.length() == 0) {
                sb.append("No campaign updates yet.\n");
            }
            updatesArea.setText(sb.toString());
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    // ---- Profile ----

    private JPanel buildProfileTab() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        form.add(new JLabel("Name"));
        form.add(profileNameField);
        form.add(new JLabel("Email"));
        form.add(profileEmailField);

        JButton save = new JButton("Save profile");
        save.addActionListener(e -> saveProfile());
        form.add(new JLabel());
        form.add(save);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    private void saveProfile() {
        try {
            String name = profileNameField.getText().trim();
            String email = profileEmailField.getText().trim();
            if (name.isEmpty() || email.isEmpty()) {
                throw new ValidationException("Name and email are required.");
            }
            User existing = userDAO.findByEmail(email);
            if (existing != null && existing.getId() != contributor.getId()) {
                throw new ValidationException("That email is already in use.");
            }
            contributor.setName(name);
            contributor.setEmail(email);
            userDAO.update(contributor);
            ActivityLogger.getInstance().log("Profile updated: " + email);
            UiUtil.info(this, "Profile updated.");
        } catch (ValidationException e) {
            UiUtil.warn(this, e.getMessage());
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    @Override
    public void onTotals(double totalRaised, double totalGoal, int campaignCount) {
        statusBar.setText(String.format("Live: %d campaigns  |  total raised %.0f of %.0f",
                campaignCount, totalRaised, totalGoal));
    }
}
