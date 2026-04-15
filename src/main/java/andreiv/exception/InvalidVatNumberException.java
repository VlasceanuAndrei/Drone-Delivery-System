package andreiv.exception;

public class InvalidVatNumberException extends RuntimeException {
    public InvalidVatNumberException(String vatNumber) {
        super("Invalid VAT number value provided for the company - " + vatNumber + ". Expected 4-15 characters, digits or uppercase letters only.");
    }
}
