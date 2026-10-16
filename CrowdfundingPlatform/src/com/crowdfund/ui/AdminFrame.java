package com.crowdfund.ui;

import com.crowdfund.db.ActivityLogDAO;
import com.crowdfund.db.CampaignDAO;
import com.crowdfund.db.SettingsDAO;
import com.crowdfund.db.UserDAO;
import com.crowdfund.exception.DataAccessException;
import com.crowdfund.model.Admin;
import com.crowdfund.model.Campaign;
import com.crowdfund.model.User;
import com.crowdfund.service.ActivityLogger;
import com.crowdfund.service.CampaignMonitor;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The admin dashboard: users, campaigns, settings and the activity log. */
public class AdminFrame extends JFrame implements CampaignMonitor.Listener {

    private final Admin admin;
    private final UserDAO userDAO = new UserDAO();
    private final CampaignDAO campaignDAO = new CampaignDAO();
    private final SettingsDAO settingsDAO = new SettingsDAO();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private final CampaignMonitor monitor = new CampaignMonitor(this);

    private final DefaultTableModel userModel =
            new DefaultTableModel(new Object[]{"ID", "Name", "Email", "Role"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
    private final DefaultTableModel campaignModel =
            new DefaultTableModel(new Object[]{"ID", "Title", "Goal", "Raised", "Status"}, 0) {
                @Override public boolean isCellEditable(int row, int col) { return false; }
            };
    private final JTable userTable = new JTable(userModel);
    private final JTable campaignTable = new JTable(campaignModel);
    private final JTextArea activityArea = new JTextArea();
    private final JPanel settingsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
    private final Map<String, JTextField> settingFields = new LinkedHashMap<>();
    private final JLabel statusBar = new JLabel(" ");

    public AdminFrame(Admin admin) {
        super(admin.getDashboardTitle());
        this.admin = admin;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(780, 580);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Users", buildUsersTab());
        tabs.addTab("Campaigns", buildCampaignsTab());
        tabs.addTab("Settings", buildSettingsTab());
        tabs.addTab("Activity", buildActivityTab());

        JLabel welcome = new JLabel("Signed in as " + admin.getName() + " (Admin)");
        welcome.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 10, 6, 10));
        statusBar.setForeground(new Color(90, 90, 90));

        add(welcome, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        refreshUsers();
        refreshCampaigns();
        refreshSettings();
        refreshActivity();
        monitor.start();
    }

    // ---- Users tab ----

    private JPanel buildUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh");
        JButton add = new JButton("Add user");
        JButton delete = new JButton("Delete selected");
        buttons.add(refresh);
        buttons.add(add);
        buttons.add(delete);
        panel.add(buttons, BorderLayout.SOUTH);

        refresh.addActionListener(e -> refreshUsers());
        add.addActionListener(e -> {
            new RegisterDialog(this, userDAO, new String[]{"Admin", "Creator", "Contributor"}).setVisible(true);
            refreshUsers();
        });
        delete.addActionListener(e -> deleteSelectedUser());
        return panel;
    }

    private void refreshUsers() {
        try {
            userModel.setRowCount(0);
            for (User user : userDAO.findAll()) {
                userModel.addRow(new Object[]{user.getId(), user.getName(), user.getEmail(), user.getRole()});
            }
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void deleteSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            UiUtil.warn(this, "Select a user first.");
            return;
        }
        int id = (int) userModel.getValueAt(row, 0);
        int choice = JOptionPane.showConfirmDialog(this, "Delete user id " + id + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            userDAO.delete(id);
            ActivityLogger.getInstance().log("Admin deleted user id " + id);
            refreshUsers();
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    // ---- Campaigns tab ----

    private JPanel buildCampaignsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(campaignTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh");
        JButton approve = new JButton("Approve selected");
        JButton reject = new JButton("Reject selected");
        buttons.add(refresh);
        buttons.add(approve);
        buttons.add(reject);
        panel.add(buttons, BorderLayout.SOUTH);

        refresh.addActionListener(e -> refreshCampaigns());
        approve.addActionListener(e -> setCampaignStatus("APPROVED"));
        reject.addActionListener(e -> setCampaignStatus("REJECTED"));
        return panel;
    }

    private void refreshCampaigns() {
        try {
            campaignModel.setRowCount(0);
            for (Campaign campaign : campaignDAO.findAll()) {
                campaignModel.addRow(new Object[]{
                        campaign.getId(),
                        campaign.getTitle(),
                        String.format("%.0f", campaign.getGoalAmount()),
                        String.format("%.0f", campaign.getRaisedAmount()),
                        campaign.getStatus()});
            }
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void setCampaignStatus(String status) {
        int row = campaignTable.getSelectedRow();
        if (row < 0) {
            UiUtil.warn(this, "Select a campaign first.");
            return;
        }
        int id = (int) campaignModel.getValueAt(row, 0);
        try {
            Campaign campaign = campaignDAO.findById(id);
            if (campaign == null) {
                UiUtil.warn(this, "Campaign not found.");
                return;
            }
            campaign.setStatus(status);
            campaignDAO.update(campaign);
            ActivityLogger.getInstance().log("Campaign " + status.toLowerCase() + ": " + campaign.getTitle());
            refreshCampaigns();
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    // ---- Settings tab ----

    private JPanel buildSettingsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JScrollPane(settingsPanel), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton reload = new JButton("Reload");
        JButton save = new JButton("Save settings");
        buttons.add(reload);
        buttons.add(save);
        panel.add(buttons, BorderLayout.SOUTH);

        reload.addActionListener(e -> refreshSettings());
        save.addActionListener(e -> saveSettings());
        return panel;
    }

    private void refreshSettings() {
        try {
            settingsPanel.removeAll();
            settingFields.clear();
            for (Map.Entry<String, String> entry : settingsDAO.findAll().entrySet()) {
                settingsPanel.add(new JLabel(entry.getKey()));
                JTextField field = new JTextField(entry.getValue());
                settingFields.put(entry.getKey(), field);
                settingsPanel.add(field);
            }
            settingsPanel.revalidate();
            settingsPanel.repaint();
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    private void saveSettings() {
        try {
            for (Map.Entry<String, JTextField> entry : settingFields.entrySet()) {
                settingsDAO.save(entry.getKey(), entry.getValue().getText().trim());
            }
            ActivityLogger.getInstance().log("System settings updated");
            UiUtil.info(this, "Settings saved.");
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    // ---- Activity tab ----

    private JPanel buildActivityTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        activityArea.setEditable(false);
        panel.add(new JScrollPane(activityArea), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh");
        buttons.add(refresh);
        panel.add(buttons, BorderLayout.SOUTH);
        refresh.addActionListener(e -> refreshActivity());
        return panel;
    }

    private void refreshActivity() {
        try {
            StringBuilder sb = new StringBuilder();
            List<String> messages = activityLogDAO.findAll();
            for (String message : messages) {
                sb.append("- ").append(message).append("\n");
            }
            activityArea.setText(sb.toString());
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    // ---- live totals from the background monitor thread ----

    @Override
    public void onTotals(double totalRaised, double totalGoal, int campaignCount) {
        statusBar.setText(String.format("Live: %d campaigns  |  total raised %.0f of %.0f",
                campaignCount, totalRaised, totalGoal));
    }
}
