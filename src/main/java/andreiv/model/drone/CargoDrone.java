package andreiv.model.drone;

import java.time.*;
import java.util.UUID;

public class CargoDrone extends Drone{
    private final boolean hasRefrigerator;

    public CargoDrone(String name, int flightRange, double maximumPayload,
                      double maximumSpeed, boolean isAvailable, LocalDate lastMaintenance, boolean hasRefrigerator) {
        super(name, flightRange, maximumPayload, maximumSpeed, isAvailable, lastMaintenance);
        this.hasRefrigerator = hasRefrigerator;
    }

    public CargoDrone(String name, int flightRange, double maximumPayload,
                      double maximumSpeed, boolean isAvailable, boolean hasRefrigerator) {
        super(name, flightRange, maximumPayload, maximumSpeed, isAvailable);
        this.hasRefrigerator = hasRefrigerator;
    }

    public CargoDrone(UUID id, String name, int flightRange, double maximumPayload,
                      double maximumSpeed, boolean isAvailable, LocalDate lastMaintenance, boolean hasRefrigerator) {
        super(id, name, flightRange, maximumPayload, maximumSpeed, isAvailable, lastMaintenance);
        this.hasRefrigerator = hasRefrigerator;
    }

    public CargoDrone(UUID id, String name, int flightRange, double maximumPayload,
                      double maximumSpeed, boolean isAvailable, boolean hasRefrigerator) {
        super(id, name, flightRange, maximumPayload, maximumSpeed, isAvailable, LocalDate.now());
        this.hasRefrigerator = hasRefrigerator;
    }

    public boolean isRefrigerated() {
        return hasRefrigerator;
    }

    @Override
    public boolean canHandleRefrigeratedPackage() { return isRefrigerated(); }

    @Override
    public boolean canHandleFragilePackage() { return false; }

    @Override
    public boolean canHandleHazardousPackage() { return true; }
}
