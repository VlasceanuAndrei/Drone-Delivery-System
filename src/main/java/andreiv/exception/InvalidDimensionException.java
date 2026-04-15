package andreiv.exception;

public class InvalidDimensionException extends RuntimeException {
    public InvalidDimensionException(double dimension) {
        super("Invalid dimension provided - " + dimension + ". Should be a positive value for each field of weight, width, length and height.");
    }
}
