package com.crowdfund.service;

import com.crowdfund.model.Admin;
import com.crowdfund.model.Contributor;
import com.crowdfund.model.Creator;
import com.crowdfund.model.User;

/**
 * Builds the correct {@link User} subclass for a role name.
 *
 * <p>This is a good example of why inheritance and polymorphism help: the DAO
 * reads a role string from the database and gets back a {@code User} object,
 * without needing to know or care which concrete class it is.</p>
 */
public class UserFactory {

    private UserFactory() {
        // utility class
    }

    public static User create(String role, int id, String name, String email, String password) {
        if ("Admin".equalsIgnoreCase(role)) {
            return new Admin(id, name, email, password);
        }
        if ("Creator".equalsIgnoreCase(role)) {
            return new Creator(id, name, email, password);
        }
        return new Contributor(id, name, email, password);
    }
}
