package statements.core.exceptions;

/**
 * Thrown when attempting to access an entry in a component that doesn't exist.
 */
public class MissingEntryException extends Exception {
    public MissingEntryException(String message) {
        super(message);
    }
}
