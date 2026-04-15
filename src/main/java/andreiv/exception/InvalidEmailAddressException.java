package andreiv.exception;

public class InvalidEmailAddressException extends RuntimeException {
    public InvalidEmailAddressException(String emailAddress) {
        super("Invalid format provided for the email address - " + emailAddress + ". Expected something like 'name@domain.com'.");
    }
}
