// User.java
// This class represents one user of the crowdfunding platform.
// A user can be an Admin, a Campaign Creator, or a Contributor.

public class User {

    private int id;           // unique id for every user
    private String name;      // full name of the user
    private String email;     // used to log in (must be unique)
    private String password;  // simple password
    private String role;      // "Admin", "Creator" or "Contributor"

    // Constructor: runs when we create a new User object with "new User(...)"
    public User(int id, String name, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters - used to read the private values from outside the class
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    // Setters - used to change the values from outside the class
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }

    // toString() decides how a User looks when we print it
    public String toString() {
        return id + " | " + name + " | " + email + " | " + role;
    }
}
