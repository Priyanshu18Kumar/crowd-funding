package com.crowdfund.ui;

import com.crowdfund.db.UserDAO;
import com.crowdfund.exception.DataAccessException;
import com.crowdfund.exception.ValidationException;
import com.crowdfund.model.User;
import com.crowdfund.service.ActivityLogger;
import com.crowdfund.service.UserFactory;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;

/** A small dialog used to create a new account. */
public class RegisterDialog extends JDialog {

    private final UserDAO userDAO;
    private final JTextField nameField = new JTextField(16);
    private final JTextField emailField = new JTextField(16);
    private final JPasswordField passwordField = new JPasswordField(16);
    private final JComboBox<String> roleBox;

    public RegisterDialog(JFrame owner, UserDAO userDAO, String[] roles) {
        super(owner, "Create account", true);
        this.userDAO = userDAO;
        this.roleBox = new JComboBox<>(roles);

        setSize(340, 320);
        setLocationRelativeTo(owner);

        JPanelHolder form = new JPanelHolder();
        form.add(new JLabel("Name"));
        form.add(nameField);
        form.add(new JLabel("Email"));
        form.add(emailField);
        form.add(new JLabel("Password"));
        form.add(passwordField);
        form.add(new JLabel("Role"));
        form.add(roleBox);

        JButton save = new JButton("Create account");
        save.addActionListener(e -> doRegister());
        form.add(save);

        add(form, BorderLayout.CENTER);
    }

    private void doRegister() {
        try {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String role = (String) roleBox.getSelectedItem();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                throw new ValidationException("All fields are required.");
            }
            if (userDAO.findByEmail(email) != null) {
                throw new ValidationException("That email is already registered.");
            }

            // UserFactory returns the right subclass for the chosen role.
            User user = UserFactory.create(role, 0, name, email, password);
            userDAO.insert(user);
            ActivityLogger.getInstance().log("New " + role + " registered: " + email);
            UiUtil.info(this, "Account created. You can now log in.");
            dispose();

        } catch (ValidationException e) {
            UiUtil.warn(this, e.getMessage());
        } catch (DataAccessException e) {
            UiUtil.error(this, e.getMessage());
        }
    }

    /** Tiny helper so the dialog body is a simple vertical form. */
    private static class JPanelHolder extends javax.swing.JPanel {
        JPanelHolder() {
            setLayout(new GridLayout(0, 1, 6, 6));
            setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 20, 16, 20));
        }
    }
}
