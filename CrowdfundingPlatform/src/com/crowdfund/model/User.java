package com.crowdfund.model;

/**
 * Abstract base class for every user of the platform.
 *
 * <p>This is where INHERITANCE and POLYMORPHISM come in:
 * {@link Admin}, {@link Creator} and {@link Contributor} all extend this
 * class, and each one provides its own version of {@link #getRole()} and
 * {@link #getDashboardTitle()}. Code that holds a {@code User} reference can
 * call those methods without knowing which subclass it really is.</p>
 */
public abstract class User {

    private int id;
    private String name;
    private String email;
    private String password;

    public User(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }

    // ---- abstract methods: every subclass MUST override these ----

    /** The role name stored in the database ("Admin", "Creator", "Contributor"). */
    public abstract String getRole();

    /** The title shown at the top of this user's dashboard window. */
    public abstract String getDashboardTitle();

    @Override
    public String toString() {
        return name + " <" + email + ">";
    }
}
