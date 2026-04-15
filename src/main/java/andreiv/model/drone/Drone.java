package andreiv.model.drone;

import java.time.*;
import andreiv.model.order.Package;
import andreiv.model.PackageRequirement;

public class Drone {
    private final String name;
    private final int flightRange;
    private final double maximumPayload;
    private final double maximumSpeed;
    private boolean isAvailable;
    private final LocalDate lastMaintenance;
    private double currentLoad;

    public Drone(String name, int flightRange, double maximumPayload,
                 double maximumSpeed, boolean isAvailable) {
        this(name, flightRange, maximumPayload, maximumSpeed, isAvailable, LocalDate.now());
    }

    public Drone(String name, int flightRange, double maximumPayload, double maximumSpeed,
                 boolean isAvailable, LocalDate lastMaintenance) {
        this.name = name;
        this.flightRange = validateFlightRange(flightRange);
        this.maximumPayload = maximumPayload;
        this.maximumSpeed = maximumSpeed;
        this.isAvailable = isAvailable;
        this.lastMaintenance = validateLastMaintenance(lastMaintenance);
        this.currentLoad = 0.0;
    }

    public String getName() {
        return name;
    }

    public int getFlightRange() {
        return flightRange;
    }

    public double getMaximumPayload() {
        return maximumPayload;
    }

    public double getMaximumSpeed() {
        return maximumSpeed;
    }

    public LocalDate getLastMaintenance() {
        return lastMaintenance;
    }

    public boolean getAvailability() {
        return isAvailable;
    }

    public double getCurrentLoad() {
        return currentLoad;
    }

    public void setAvailability(boolean availability) {
        isAvailable = availability;
    }

    private static int validateFlightRange(int flightRange) {
        if (flightRange < 0 || flightRange > 550) {
            throw new IllegalArgumentException("Invalid range provided for flightRange.");
        }
        return flightRange;
    }

    private static LocalDate validateLastMaintenance(LocalDate lastMaintenance) {
        final int currentYear = LocalDate.now().getYear();
        if (currentYear - lastMaintenance.getYear() > 10 || currentYear - lastMaintenance.getYear() < 0) {
            throw new IllegalArgumentException("Invalid date provided for lastMaintenance.");
        }
        return lastMaintenance;
    }

    public boolean canReach(double distance) {
        return distance <= flightRange;
    }

    public boolean canCarry(Package pkg) {
        return pkg.getWeight() <= maximumPayload - currentLoad;
    }

    public void addWeight(double weight) {
        currentLoad += weight;
    }

    public boolean satisfiesPackageRequirements(Package pkg) {
        for (PackageRequirement r : pkg.getRequirements()) {
            if (!requirementSatisfied(r)) {
                return false;
            }
        }
        return true;
    }

    private boolean requirementSatisfied(PackageRequirement r) {
        return switch (r) {
            case REFRIGERATED -> canHandleRefrigeratedPackage();
            case EXPRESS_DELIVERY -> canHandleExpressDelivery();
            case FRAGILE -> canHandleFragilePackage();
            case HAZARDOUS -> canHandleHazardousPackage();
            default -> true;
        };
    }

    protected boolean canHandleRefrigeratedPackage() { return false; }

    protected boolean canHandleExpressDelivery() { return false; }

    protected boolean canHandleFragilePackage() { return true; }

    protected boolean canHandleHazardousPackage() { return false; }
}
