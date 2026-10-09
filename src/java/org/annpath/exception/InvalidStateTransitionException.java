package org.annpath.exception;

public class InvalidStateTransitionException extends Exception {
    private static final long serialVersionUID = 1L;

    private String fromState;
    private String toState;

    public InvalidStateTransitionException(String currentState, String attemptedState, String reason) {
        super("Invalid state transition from [" + currentState + "] to [" + attemptedState + "]: " + reason);
        this.fromState = currentState;
        this.toState = attemptedState;
    }

    public InvalidStateTransitionException(String message) {
        super(message);
    }

    public String getFromState() {
        return fromState;
    }

    public String getToState() {
        return toState;
    }
}
