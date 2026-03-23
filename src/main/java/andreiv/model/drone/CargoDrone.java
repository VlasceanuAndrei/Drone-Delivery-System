package andreiv.model.drone;

import java.time.*;

public class CargoDrone extends Drone{
    private final boolean hasRefrigerator;

    public CargoDrone(String name, int flightRange, double maximumPayload,
                      double maximumSpeed, LocalDate dateOfPurchase, boolean hasRefrigerator) {
        super(name, flightRange, maximumPayload, maximumSpeed, dateOfPurchase);
        this.hasRefrigerator = hasRefrigerator;
    }
}
