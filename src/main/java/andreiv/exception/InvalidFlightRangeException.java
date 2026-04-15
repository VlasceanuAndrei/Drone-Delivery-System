package andreiv.exception;

public class InvalidFlightRangeException extends RuntimeException {
    public InvalidFlightRangeException(int flightRange) {
        super("Invalid range provided for flightRange - " + flightRange + ". Should be a value between 0 and 550.");
    }
}
