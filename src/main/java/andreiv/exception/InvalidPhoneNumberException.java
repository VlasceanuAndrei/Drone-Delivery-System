package andreiv.exception;

public class InvalidPhoneNumberException extends RuntimeException {
    public InvalidPhoneNumberException(String phoneNumber) {
        super("Invalid phone number provided - " + phoneNumber + ". Expected '+' followed by 7-15 digits.");
    }
}
