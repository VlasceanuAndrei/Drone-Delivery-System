package andreiv.model.drone;

import andreiv.model.DroneType;
import java.time.*;
import java.util.*;

public class DroneFactory {
    public static Drone createDrone(DroneType type, String name, int flightRange, double maximumPayload,
                             double maximumSpeed, boolean isAvailable, Optional<LocalDate> lastMaintenance,
                             boolean hasRefrigerator) {
        LocalDate maintenance = lastMaintenance.orElse(LocalDate.now());
        return switch (type) {
            case NORMAL -> lastMaintenance.isPresent() ?
                    new Drone(name, flightRange, maximumPayload, maximumSpeed, isAvailable, maintenance) :
                    new Drone(name, flightRange, maximumPayload, maximumSpeed, isAvailable);

            case CARGO -> lastMaintenance.isPresent() ?
                    new CargoDrone(name, flightRange, maximumPayload, maximumSpeed, isAvailable,
                            maintenance, hasRefrigerator) :
                    new CargoDrone(name, flightRange, maximumPayload, maximumSpeed, isAvailable, hasRefrigerator);

            case HIGH_SPEED -> lastMaintenance.isPresent() ?
                    new HighSpeedDrone(name, flightRange, maximumPayload, maximumSpeed,
                            isAvailable, maintenance) :
                    new HighSpeedDrone(name, flightRange, maximumPayload, maximumSpeed, isAvailable);
        };
    }

    public static Drone createDroneWithId(UUID id, DroneType type, String name, int flightRange, double maximumPayload,
                                    double maximumSpeed, boolean isAvailable, Optional<LocalDate> lastMaintenance,
                                    boolean hasRefrigerator) {
        LocalDate maintenance = lastMaintenance.orElse(LocalDate.now());
        return switch (type) {
            case NORMAL -> lastMaintenance.isPresent() ?
                    new Drone(id, name, flightRange, maximumPayload, maximumSpeed, isAvailable, maintenance) :
                    new Drone(id, name, flightRange, maximumPayload, maximumSpeed, isAvailable);

            case CARGO -> lastMaintenance.isPresent() ?
                    new CargoDrone(id, name, flightRange, maximumPayload, maximumSpeed, isAvailable,
                            maintenance, hasRefrigerator) :
                    new CargoDrone(id, name, flightRange, maximumPayload, maximumSpeed, isAvailable, hasRefrigerator);

            case HIGH_SPEED -> lastMaintenance.isPresent() ?
                    new HighSpeedDrone(id, name, flightRange, maximumPayload, maximumSpeed,
                            isAvailable, maintenance) :
                    new HighSpeedDrone(id, name, flightRange, maximumPayload, maximumSpeed, isAvailable);
        };
    }
}
