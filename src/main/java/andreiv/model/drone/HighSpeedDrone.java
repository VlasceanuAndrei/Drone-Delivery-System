package andreiv.model.drone;

import java.time.*;

public class HighSpeedDrone extends Drone {
    private final boolean hasBoostMode;

    public HighSpeedDrone(String name, int flightRange, double maximumPayload,
                          double maximumSpeed, boolean isAvailable, LocalDate dateOfPurchase, boolean hasBoostMode) {
        super(name, flightRange, maximumPayload, maximumSpeed, isAvailable, dateOfPurchase);
        this.hasBoostMode = hasBoostMode;
    }

    public HighSpeedDrone(String name, int flightRange, double maximumPayload,
                          double maximumSpeed, boolean isAvailable, boolean hasBoostMode) {
        super(name, flightRange, maximumPayload, maximumSpeed, isAvailable);
        this.hasBoostMode = hasBoostMode;
    }

    public void startThrusters() {
        if (!hasBoostMode) {
            throw new IllegalStateException("Boost mode is not available for this drone.");
        } else {
            System.out.println("Boost mode activated.");
        }
    }

    @Override
    public boolean canHandleExpressDelivery() { return true; }

    @Override
    public boolean canHandleFragilePackage() { return false; }
}
