package andreiv.model.drone;

import java.time.*;

public class Drone {
    private String name;
    private final int flightRange;
    private double maximumPayload;
    private double maximumSpeed;
    private boolean isAvailable;
    private final LocalDate dateOfPurchase;

    public Drone(String name, int flightRange, double maximumPayload,
                 double maximumSpeed, boolean isAvailable) {
        this.name = name;
        this.flightRange = validateFlightRange(flightRange);
        this.maximumPayload = maximumPayload;
        this.maximumSpeed = maximumSpeed;
        this.isAvailable = isAvailable;
        this.dateOfPurchase = validateDateOfPurchase(LocalDate.now());
    }

    public Drone(String name, int flightRange, double maximumPayload, double maximumSpeed,
                 boolean isAvailable, LocalDate dateOfPurchase) {
        this.name = name;
        this.flightRange = validateFlightRange(flightRange);
        this.maximumPayload = maximumPayload;
        this.maximumSpeed = maximumSpeed;
        this.isAvailable = isAvailable;
        this.dateOfPurchase = validateDateOfPurchase(dateOfPurchase);
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

    public boolean getAvailability() {
        return isAvailable;
    }

    private static int validateFlightRange(int flightRange) {
        if (flightRange < 0 || flightRange > 550) {
            throw new IllegalArgumentException("Invalid range provided for flightRange.");
        }
        return flightRange;
    }

    private static LocalDate validateDateOfPurchase(LocalDate dateOfPurchase) {
        final int currentYear = LocalDate.now().getYear();
        if (currentYear - dateOfPurchase.getYear() > 10 || currentYear - dateOfPurchase.getYear() < 0) {
            throw new IllegalArgumentException("Invalid year provided for dateOfPurchase.");
        }
        return dateOfPurchase;
    }
}
