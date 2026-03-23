package andreiv.model.drone;

import java.time.*;

public class Drone {
    private String name;
    private final int flightRange;
    private double maximumPayload;
    private double maximumSpeed;
    private final LocalDate dateOfPurchase;

    public Drone(String name, int flightRange, double maximumPayload,
                 double maximumSpeed) {
        this.name = name;
        this.flightRange = validateFlightRange(flightRange);
        this.maximumPayload = maximumPayload;
        this.maximumSpeed = maximumSpeed;
        this.dateOfPurchase = setDateOfPurchase(LocalDate.now());
    }

    public Drone(String name, int flightRange, double maximumPayload,
                 double maximumSpeed, LocalDate dateOfPurchase) {
        this.name = name;
        this.flightRange = validateFlightRange(flightRange);
        this.maximumPayload = maximumPayload;
        this.maximumSpeed = maximumSpeed;
        this.dateOfPurchase = setDateOfPurchase(dateOfPurchase);
    }

    public int getFlightRange() {
        return flightRange;
    }

    public double[] getMaximumDroneCapacities() {
        return new double[]{maximumPayload, maximumSpeed};
    }

    private static int validateFlightRange(int flightRange) {
        if (flightRange < 0 || flightRange > 550) {
            throw new IllegalArgumentException("Invalid range provided for flightRange.");
        }
        return flightRange;
    }

    private static LocalDate setDateOfPurchase(LocalDate dateOfPurchase) {
        final int currentYear = LocalDate.now().getYear();
        if (currentYear - dateOfPurchase.getYear() > 10 || currentYear - dateOfPurchase.getYear() < 0) {
            throw new IllegalArgumentException("Invalid year provided for dateOfPurchase.");
        }
        return dateOfPurchase;
    }
}
