package com.crowdfund.model;

/** An administrator: manages users, campaigns and system settings. */
public class Admin extends User {

    public Admin(int id, String name, String email, String password) {
        super(id, name, email, password);
    }

    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public String getDashboardTitle() {
        return "Admin Dashboard";
    }
}
