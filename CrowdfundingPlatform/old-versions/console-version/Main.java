// Main.java
// This is the file we run. It shows menus on the screen and reads what the
// user types. Depending on the role (Admin / Creator / Contributor) it shows
// a different dashboard.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    // One Scanner is shared by the whole program to read keyboard input
    static Scanner sc = new Scanner(System.in);

    // The single platform object that holds all our data
    static CrowdfundingPlatform platform = new CrowdfundingPlatform();

    // ------------------- Starting point -------------------

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("        WELCOME TO THE CROWDFUNDING PLATFORM");
        System.out.println("=================================================");

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("1. Login");
            System.out.println("2. Register (Creator / Contributor)");
            System.out.println("3. Exit");
            int choice = readInt("Enter your choice: ");

            if (choice == 1) {
                login();
            } else if (choice == 2) {
                register();
            } else if (choice == 3) {
                running = false;
                System.out.println("Thank you for using the platform. Goodbye!");
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ------------------- Login -------------------

    static void login() {
        System.out.println("\n--- LOGIN ---");
        String email = readLine("Email: ");
        String password = readLine("Password: ");

        User user = platform.findUserByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            System.out.println("Invalid email or password.");
            return;
        }

        System.out.println("Welcome, " + user.getName() + " (" + user.getRole() + ")");
        platform.log("User logged in: " + user.getEmail());

        // Show the right dashboard for the role
        if (user.getRole().equals("Admin")) {
            adminMenu(user);
        } else if (user.getRole().equals("Creator")) {
            creatorMenu(user);
        } else {
            contributorMenu(user);
        }
    }

    // ------------------- Register -------------------

    static void register() {
        System.out.println("\n--- REGISTER ---");
        String name = readLine("Name: ");
        String email = readLine("Email: ");

        if (platform.findUserByEmail(email) != null) {
            System.out.println("A user with this email already exists.");
            return;
        }

        String password = readLine("Password: ");
        System.out.println("Choose a role: 1. Campaign Creator   2. Contributor");
        int roleChoice = readInt("Enter choice: ");

        String role;
        if (roleChoice == 1) {
            role = "Creator";
        } else {
            role = "Contributor";
        }

        platform.addUser(name, email, password, role);
        platform.log("New " + role + " registered: " + email);
        System.out.println("Registration successful! You can now log in.");
    }

    // ------------------- Admin dashboard -------------------

    static void adminMenu(User admin) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- ADMIN DASHBOARD ---");
            System.out.println("1. Manage Users");
            System.out.println("2. Manage Campaigns");
            System.out.println("3. System Settings");
            System.out.println("4. Activity Monitoring");
            System.out.println("5. Logout");
            int choice = readInt("Enter choice: ");

            if (choice == 1) {
                manageUsers();
            } else if (choice == 2) {
                manageCampaigns();
            } else if (choice == 3) {
                systemSettings();
            } else if (choice == 4) {
                viewActivity();
            } else if (choice == 5) {
                loggedIn = false;
                System.out.println("Logged out.");
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    static void manageUsers() {
        System.out.println("\n--- USER MANAGEMENT ---");
        ArrayList<User> users = platform.getUsers();
        System.out.println("ID | Name | Email | Role");
        for (int i = 0; i < users.size(); i++) {
            System.out.println(users.get(i));
        }

        System.out.println("\n1. Add User   2. Update User   3. Delete User   4. Back");
        int choice = readInt("Enter choice: ");

        if (choice == 1) {
            String name = readLine("Name: ");
            String email = readLine("Email: ");
            if (platform.findUserByEmail(email) != null) {
                System.out.println("Email already in use.");
                return;
            }
            String password = readLine("Password: ");
            String role = readLine("Role (Admin/Creator/Contributor): ");
            platform.addUser(name, email, password, role);
            platform.log("Admin added user: " + email);
            System.out.println("User created successfully.");
        } else if (choice == 2) {
            String email = readLine("Email of user to update: ");
            User u = platform.findUserByEmail(email);
            if (u == null) {
                System.out.println("User not found.");
                return;
            }
            u.setName(readLine("New name: "));
            u.setPassword(readLine("New password: "));
            u.setRole(readLine("New role: "));
            platform.log("Admin updated user: " + email);
            System.out.println("User updated successfully.");
        } else if (choice == 3) {
            String email = readLine("Email of user to delete: ");
            if (platform.deleteUser(email)) {
                platform.log("Admin deleted user: " + email);
                System.out.println("User deleted successfully.");
            } else {
                System.out.println("User not found.");
            }
        }
    }

    static void manageCampaigns() {
        System.out.println("\n--- CAMPAIGN MANAGEMENT ---");
        ArrayList<Campaign> campaigns = platform.getCampaigns();
        if (campaigns.size() == 0) {
            System.out.println("No campaigns have been created yet.");
            return;
        }
        for (int i = 0; i < campaigns.size(); i++) {
            System.out.println(campaigns.get(i));
        }

        System.out.println("\n1. Approve Campaign   2. Reject Campaign   3. Back");
        int choice = readInt("Enter choice: ");

        if (choice == 1 || choice == 2) {
            int id = readInt("Enter campaign ID: ");
            Campaign c = platform.findCampaignById(id);
            if (c == null) {
                System.out.println("Campaign not found.");
                return;
            }
            if (choice == 1) {
                c.setStatus("APPROVED");
                platform.log("Campaign approved: " + c.getTitle());
                System.out.println("Campaign approved.");
            } else {
                c.setStatus("REJECTED");
                platform.log("Campaign rejected: " + c.getTitle());
                System.out.println("Campaign rejected.");
            }
        }
    }

    static void systemSettings() {
        System.out.println("\n--- SYSTEM SETTINGS ---");
        HashMap<String, String> settings = platform.getSettings();
        for (String key : settings.keySet()) {
            System.out.println(key + " : " + settings.get(key));
        }

        System.out.println("\n1. Update a setting   2. Back");
        int choice = readInt("Enter choice: ");
        if (choice == 1) {
            String key = readLine("Setting name: ");
            String value = readLine("New value: ");
            platform.updateSetting(key, value);
            platform.log("Setting updated: " + key);
            System.out.println("Settings updated successfully.");
        }
    }

    static void viewActivity() {
        System.out.println("\n--- ACTIVITY MONITORING ---");
        ArrayList<String> log = platform.getActivityLog();
        if (log.size() == 0) {
            System.out.println("No activity recorded yet.");
            return;
        }
        for (int i = 0; i < log.size(); i++) {
            System.out.println((i + 1) + ". " + log.get(i));
        }
    }

    // ------------------- Creator dashboard -------------------

    static void creatorMenu(User creator) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- CAMPAIGN CREATOR DASHBOARD ---");
            System.out.println("1. Create Campaign");
            System.out.println("2. My Campaigns");
            System.out.println("3. Update Campaign Progress");
            System.out.println("4. Message Contributors");
            System.out.println("5. Campaign Analytics");
            System.out.println("6. Logout");
            int choice = readInt("Enter choice: ");

            if (choice == 1) {
                createCampaign(creator);
            } else if (choice == 2) {
                viewMyCampaigns(creator);
            } else if (choice == 3) {
                updateProgress(creator);
            } else if (choice == 4) {
                messageContributors(creator);
            } else if (choice == 5) {
                campaignAnalytics(creator);
            } else if (choice == 6) {
                loggedIn = false;
                System.out.println("Logged out.");
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    static void createCampaign(User creator) {
        System.out.println("\n--- CREATE CAMPAIGN ---");
        String title = readLine("Title: ");
        String description = readLine("Description: ");
        double goal = readDouble("Goal amount: ");

        Campaign c = platform.createCampaign(title, description, goal, creator.getEmail());
        platform.log("Campaign created: " + title + " by " + creator.getEmail());
        System.out.println("Campaign created successfully. It is now waiting for admin approval.");
        System.out.println("Your campaign ID is " + c.getId() + ".");
    }

    static void viewMyCampaigns(User creator) {
        System.out.println("\n--- MY CAMPAIGNS ---");
        ArrayList<Campaign> list = platform.getCampaignsByCreator(creator.getEmail());
        if (list.size() == 0) {
            System.out.println("You have not created any campaigns yet.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Campaign c = list.get(i);
            System.out.println(c);
            System.out.println("   Progress: " + String.format("%.1f", c.getProgress()) + "%");
        }
    }

    static void updateProgress(User creator) {
        System.out.println("\n--- UPDATE CAMPAIGN PROGRESS ---");
        int id = readInt("Enter campaign ID: ");
        Campaign c = platform.findCampaignById(id);
        if (c == null || !c.getCreatorEmail().equalsIgnoreCase(creator.getEmail())) {
            System.out.println("Campaign not found or not yours.");
            return;
        }
        String message = readLine("Enter progress update message: ");
        c.addUpdate(message);
        platform.log("Progress update posted on: " + c.getTitle());
        System.out.println("Progress update posted successfully.");
    }

    static void messageContributors(User creator) {
        System.out.println("\n--- MESSAGE CONTRIBUTORS ---");
        int id = readInt("Enter campaign ID: ");
        Campaign c = platform.findCampaignById(id);
        if (c == null || !c.getCreatorEmail().equalsIgnoreCase(creator.getEmail())) {
            System.out.println("Campaign not found or not yours.");
            return;
        }
        String message = readLine("Enter your message: ");
        ArrayList<Contribution> cons = platform.getContributionsByCampaign(c.getId());
        platform.log("Message sent to contributors of: " + c.getTitle());
        System.out.println("Message delivered to " + cons.size() + " contributor(s).");
    }

    static void campaignAnalytics(User creator) {
        System.out.println("\n--- CAMPAIGN ANALYTICS ---");
        ArrayList<Campaign> list = platform.getCampaignsByCreator(creator.getEmail());
        if (list.size() == 0) {
            System.out.println("No campaigns to analyse.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Campaign c = list.get(i);
            ArrayList<Contribution> cons = platform.getContributionsByCampaign(c.getId());
            double total = 0;
            for (int j = 0; j < cons.size(); j++) {
                total = total + cons.get(j).getAmount();
            }
            System.out.println("Campaign: " + c.getTitle() + " (" + c.getStatus() + ")");
            System.out.println("   Contributors: " + cons.size());
            System.out.println("   Total raised: " + total + " / " + c.getGoalAmount());
            System.out.println("   Progress: " + String.format("%.1f", c.getProgress()) + "%");
        }
    }

    // ------------------- Contributor dashboard -------------------

    static void contributorMenu(User contributor) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- CONTRIBUTOR DASHBOARD ---");
            System.out.println("1. View Campaigns");
            System.out.println("2. Contribute to a Campaign");
            System.out.println("3. My Contributions");
            System.out.println("4. Campaign Updates");
            System.out.println("5. Manage Profile");
            System.out.println("6. Logout");
            int choice = readInt("Enter choice: ");

            if (choice == 1) {
                viewApprovedCampaigns();
            } else if (choice == 2) {
                contribute(contributor);
            } else if (choice == 3) {
                myContributions(contributor);
            } else if (choice == 4) {
                campaignUpdates();
            } else if (choice == 5) {
                manageProfile(contributor);
            } else if (choice == 6) {
                loggedIn = false;
                System.out.println("Logged out.");
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    static void viewApprovedCampaigns() {
        System.out.println("\n--- APPROVED CAMPAIGNS ---");
        ArrayList<Campaign> list = platform.getApprovedCampaigns();
        if (list.size() == 0) {
            System.out.println("No campaigns are open for contribution right now.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Campaign c = list.get(i);
            System.out.println(c);
            System.out.println("   " + c.getDescription());
            System.out.println("   Progress: " + String.format("%.1f", c.getProgress()) + "%");
        }
    }

    static void contribute(User contributor) {
        System.out.println("\n--- CONTRIBUTE ---");
        int id = readInt("Enter campaign ID: ");
        Campaign c = platform.findCampaignById(id);
        if (c == null || !c.getStatus().equals("APPROVED")) {
            System.out.println("Campaign not found or not open for contribution.");
            return;
        }
        double amount = readDouble("Enter amount to contribute: ");
        if (amount <= 0) {
            System.out.println("Amount must be greater than zero.");
            return;
        }

        // A simple pretend payment step (no real payment gateway here)
        System.out.println("Processing payment of " + amount + " ...");

        platform.addContribution(c, contributor.getEmail(), amount);
        platform.log("Contribution of " + amount + " to " + c.getTitle());
        System.out.println("Contribution confirmed. Thank you!");
    }

    static void myContributions(User contributor) {
        System.out.println("\n--- MY CONTRIBUTIONS ---");
        ArrayList<Contribution> list = platform.getContributionsByUser(contributor.getEmail());
        if (list.size() == 0) {
            System.out.println("You have not contributed to any campaign yet.");
            return;
        }
        double total = 0;
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
            total = total + list.get(i).getAmount();
        }
        System.out.println("Total contributed: " + total);
    }

    static void campaignUpdates() {
        System.out.println("\n--- CAMPAIGN UPDATES ---");
        ArrayList<Campaign> list = platform.getApprovedCampaigns();
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            Campaign c = list.get(i);
            ArrayList<String> updates = c.getUpdates();
            if (updates.size() > 0) {
                found = true;
                System.out.println("Campaign: " + c.getTitle());
                for (int j = 0; j < updates.size(); j++) {
                    System.out.println("   - " + updates.get(j));
                }
            }
        }
        if (!found) {
            System.out.println("There are no campaign updates yet.");
        }
    }

    static void manageProfile(User contributor) {
        System.out.println("\n--- MANAGE PROFILE ---");
        System.out.println("Current details: " + contributor);
        System.out.println("1. Update Name   2. Update Email   3. Update Password   4. Back");
        int choice = readInt("Enter choice: ");

        if (choice == 1) {
            contributor.setName(readLine("New name: "));
            System.out.println("Profile updated successfully.");
        } else if (choice == 2) {
            String newEmail = readLine("New email: ");
            if (platform.findUserByEmail(newEmail) != null) {
                System.out.println("That email is already in use.");
                return;
            }
            contributor.setEmail(newEmail);
            System.out.println("Profile updated successfully.");
        } else if (choice == 3) {
            contributor.setPassword(readLine("New password: "));
            System.out.println("Profile updated successfully.");
        }
    }

    // ------------------- Small input helper methods -------------------

    // Read a whole line of text
    static String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    // Read a number; keep asking until the user types a valid one
    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // Read a decimal number; keep asking until valid
    static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
