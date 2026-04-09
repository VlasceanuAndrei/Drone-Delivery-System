package andreiv.model.drone;

import java.time.*;

public class CargoDrone extends Drone{
    private final boolean hasRefrigerator;

    public CargoDrone(String name, int flightRange, double maximumPayload,
                      double maximumSpeed, boolean isAvailable, LocalDate dateOfPurchase, boolean hasRefrigerator) {
        super(name, flightRange, maximumPayload, maximumSpeed, isAvailable, dateOfPurchase);
        this.hasRefrigerator = hasRefrigerator;
    }

    public CargoDrone(String name, int flightRange, double maximumPayload,
                      double maximumSpeed, boolean isAvailable, boolean hasRefrigerator) {
        super(name, flightRange, maximumPayload, maximumSpeed, isAvailable);
        this.hasRefrigerator = hasRefrigerator;
    }

    public boolean isRefrigerated() {
        return hasRefrigerator;
    }

    @Override
    protected boolean canHandleRefrigeratedPackage() { return isRefrigerated(); }

    @Override
    protected boolean canHandleFragilePackage() { return false; }

    @Override
    protected boolean canHandleHazardousPackage() { return true; }
}
