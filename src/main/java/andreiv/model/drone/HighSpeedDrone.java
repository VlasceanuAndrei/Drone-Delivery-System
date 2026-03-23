package andreiv.model.drone;

import java.time.*;

public class HighSpeedDrone extends Drone{
    private final boolean hasBoostMode;

    public HighSpeedDrone(String name, int flightRange, double maximumPayload,
                          double maximumSpeed, LocalDate dateOfPurchase, boolean hasBoostMode) {
        super(name, flightRange, maximumPayload, maximumSpeed, dateOfPurchase);
        this.hasBoostMode = hasBoostMode;
    }

    public void startThrusters() {
        if (!hasBoostMode) {
            throw new IllegalStateException("Boost mode is not available for this drone.");
        } else {
            System.out.println("Boost mode activated.");
        }
    }

}
