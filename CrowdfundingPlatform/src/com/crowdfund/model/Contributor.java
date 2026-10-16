package com.crowdfund.model;

/** A contributor: supports campaigns and tracks their contributions. */
public class Contributor extends User {

    public Contributor(int id, String name, String email, String password) {
        super(id, name, email, password);
    }

    @Override
    public String getRole() {
        return "Contributor";
    }

    @Override
    public String getDashboardTitle() {
        return "Contributor Dashboard";
    }
}
