package org.annpath.exception;

public class ConcurrencyException extends Exception {
    private static final long serialVersionUID = 1L;

    public ConcurrencyException(String message) {
        super(message);
    }

    public ConcurrencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
