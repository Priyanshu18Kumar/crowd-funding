package com.crowdfund.model;

/** A campaign creator: creates campaigns and communicates with contributors. */
public class Creator extends User {

    public Creator(int id, String name, String email, String password) {
        super(id, name, email, password);
    }

    @Override
    public String getRole() {
        return "Creator";
    }

    @Override
    public String getDashboardTitle() {
        return "Campaign Creator Dashboard";
    }
}
