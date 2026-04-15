package andreiv.exception;

public class CoordinatesNotFoundException extends RuntimeException {
    public CoordinatesNotFoundException(String city, String type) {
        super("Couldn't provide coordinates for " + city + " (type of location: " + type + ").");
    }
}
