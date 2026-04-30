package andreiv.model.drone;

import java.time.*;
import java.util.UUID;

public class HighSpeedDrone extends Drone {
    public HighSpeedDrone(String name, int flightRange, double maximumPayload,
                          double maximumSpeed, boolean isAvailable, LocalDate dateOfPurchase) {
        super(name, flightRange, maximumPayload, maximumSpeed, isAvailable, dateOfPurchase);
    }

    public HighSpeedDrone(String name, int flightRange, double maximumPayload,
                          double maximumSpeed, boolean isAvailable) {
        super(name, flightRange, maximumPayload, maximumSpeed, isAvailable);
    }

    public HighSpeedDrone(UUID id, String name, int flightRange, double maximumPayload,
                          double maximumSpeed, boolean isAvailable, LocalDate dateOfPurchase) {
        super(id, name, flightRange, maximumPayload, maximumSpeed, isAvailable, dateOfPurchase);
    }

    public HighSpeedDrone(UUID id, String name, int flightRange, double maximumPayload,
                          double maximumSpeed, boolean isAvailable) {
        super(id, name, flightRange, maximumPayload, maximumSpeed, isAvailable, LocalDate.now());
    }

    @Override
    public boolean canHandleExpressDelivery() { return true; }

    @Override
    public boolean canHandleFragilePackage() { return false; }
}
