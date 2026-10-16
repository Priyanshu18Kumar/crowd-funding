package com.crowdfund.exception;

/**
 * A custom CHECKED exception thrown by every database class when something
 * goes wrong talking to the database. Wrapping the raw SQLException keeps the
 * rest of the program free of JDBC details.
 */
public class DataAccessException extends Exception {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
