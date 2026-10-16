package com.crowdfund.ui;

import com.crowdfund.db.UserDAO;
import com.crowdfund.exception.DataAccessException;
import com.crowdfund.exception.ValidationException;
import com.crowdfund.model.Admin;
import com.crowdfund.model.Contributor;
import com.crowdfund.model.Creator;
import com.crowdfund.model.User;
import com.crowdfund.service.ActivityLogger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

/** The first window: log in or open the register dialog. */
public class LoginFrame extends JFrame {

    private final UserDAO userDAO = new UserDAO();
    private final JTextField emailField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);

    public LoginFrame() {
        super("CrowdFund - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 340);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        JLabel title = new JLabel("CrowdFund", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(new Color(47, 111, 237));
        main.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Email"));
        form.add(emailField);
        form.add(new JLabel("Password"));
        form.add(passwordField);

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 8));
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        buttons.add(loginButton);
        buttons.add(registerButton);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(form, BorderLayout.CENTER);
        center.add(buttons, BorderLayout.SOUTH);
        main.add(center, BorderLayout.CENTER);

        JLabel hint = new JLabel("Default admin: admin@crowd.com / admin123");
        hint.setFont(new Font("Arial", Font.PLAIN, 11));
        hint.setForeground(Color.GRAY);
        main.add(hint, BorderLayout.SOUTH);

        add(main);

        loginButton.addActionListener(e -> doLogin());
        registerButton.addActionListener(e ->
                new RegisterDialog(this, userDAO, new String[]{"Creator", "Contributor"}).setVisible(true));
        getRootPane().setDefaultButton(loginButton);
    }

    private void doLogin() {
        try {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                throw new ValidationException("Please enter both email and password.");
            }

            User user = userDAO.findByEmail(email);
            if (user == null || !user.getPassword().equals(password)) {
                UiUtil.warn(this, "Invalid email or password.");
                return;
            }

            ActivityLogger.getInstance().log("User logged in: " + user.getEmail());
            openDashboard(user);

        } catch (ValidationException e) {
            UiUtil.warn(this, e.getMessage());
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    /** POLYMORPHISM in action: one User reference, three different dashboards. */
    private void openDashboard(User user) {
        JFrame dashboard;
        if (user instanceof Admin) {
            dashboard = new AdminFrame((Admin) user);
        } else if (user instanceof Creator) {
            dashboard = new CreatorFrame((Creator) user);
        } else {
            dashboard = new ContributorFrame((Contributor) user);
        }
        dashboard.setVisible(true);
        dispose();
    }
}
