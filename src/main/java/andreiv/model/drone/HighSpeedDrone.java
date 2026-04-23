package andreiv.model.drone;

import java.time.*;

public class HighSpeedDrone extends Drone {
    public HighSpeedDrone(String name, int flightRange, double maximumPayload,
                          double maximumSpeed, boolean isAvailable, LocalDate dateOfPurchase) {
        super(name, flightRange, maximumPayload, maximumSpeed, isAvailable, dateOfPurchase);
    }

    public HighSpeedDrone(String name, int flightRange, double maximumPayload,
                          double maximumSpeed, boolean isAvailable) {
        super(name, flightRange, maximumPayload, maximumSpeed, isAvailable);
    }

    @Override
    public boolean canHandleExpressDelivery() { return true; }

    @Override
    public boolean canHandleFragilePackage() { return false; }
}
