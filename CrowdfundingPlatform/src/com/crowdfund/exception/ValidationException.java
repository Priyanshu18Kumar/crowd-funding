package com.crowdfund.exception;

/**
 * Thrown when the data a user entered is not valid (empty fields, a number
 * that is not a number, a negative amount, and so on).
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
