package andreiv.exception;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InvalidMaintenanceDateException extends RuntimeException {
    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    public InvalidMaintenanceDateException(LocalDate lastMaintenance) {
        super("Invalid date provided for lastMaintenance - " + lastMaintenance.format(FORMATTER) + ". Date must not be in the future and must be within the last 10 years.");
    }
}
