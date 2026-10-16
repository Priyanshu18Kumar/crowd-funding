package com.crowdfund.db;

import com.crowdfund.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Handles the JDBC connection to the database.
 *
 * <p>The connection details default to a local MySQL server, but they can be
 * overridden from the command line with system properties, for example:</p>
 *
 * <pre>
 *   java -Ddb.url="jdbc:mysql://localhost:3306/crowdfund" \
 *        -Ddb.user=root -Ddb.password=yourpassword com.crowdfund.Main
 * </pre>
 *
 * <p>The override is also handy for testing with a different database.</p>
 */
public class DBConnection {

    private static final String URL = System.getProperty(
            "db.url",
            "jdbc:mysql://localhost:3306/crowdfund?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
    private static final String USER = System.getProperty("db.user", "root");
    private static final String PASSWORD = System.getProperty("db.password", "root");

    private DBConnection() {
        // utility class - no instances
    }

    /** Opens a new connection. The caller must close it (use try-with-resources). */
    public static Connection getConnection() throws DataAccessException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Could not connect to the database. Check that MySQL is running and "
                            + "that the database 'crowdfund' exists. (" + e.getMessage() + ")", e);
        }
    }

    /** Small check used at start-up so we can show a friendly message. */
    public static void testConnection() throws DataAccessException {
        try (Connection connection = getConnection()) {
            if (connection == null) {
                throw new DataAccessException("No database connection was returned.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database connection failed: " + e.getMessage(), e);
        }
    }
}
