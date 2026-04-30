package andreiv.model.hub;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.time.LocalDate;
import java.util.UUID;

import andreiv.model.drone.*;
import andreiv.model.order.*;
import andreiv.model.order.Package;
import andreiv.model.personnel.*;
import andreiv.model.PackageRequirement;
import andreiv.model.PersonnelCertification;

public class DroneHub {
    private final UUID id;
    private final String name;
    private final List<Drone> fleet;
    private final List<Order> orders;
    private final List<Personnel> crew;
    private final Address address;
    private final List<Drone> dronesUnderMaintenance;
    private final static int MAINTENANCE_THRESHOLD = 18;

    public DroneHub(String name, Address address) {
        this(UUID.randomUUID(), name, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), address);
    }

    public DroneHub(String name, List<Drone> fleet, List<Order> orders,
                    List<Personnel> crew, Address address) {
        this(UUID.randomUUID(), name, fleet, orders, crew, address);
    }

    public DroneHub(UUID id, String name, List<Drone> fleet, List<Order> orders,
                    List<Personnel> crew, Address address) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.name = name;
        this.fleet = fleet;
        this.orders = orders;
        this.crew = crew;
        this.address = address;
        this.dronesUnderMaintenance = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Drone> getFleet() {
        return fleet;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public List<Personnel> getCrew() {
        return crew;
    }

    public Address getAddress() {
        return address;
    }

    public void addDrone(Drone drone) {
        fleet.add(drone);
    }

    public void removeDrone(Drone drone) {
        fleet.remove(drone);
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }

    public void addPersonnel(Personnel member) {
        crew.add(member);
    }

    public void removePersonnel(Personnel member) {
        crew.remove(member);
    }

    public List<Drone> getAvailableDrones() {
        List<Drone> availableDrones = new ArrayList<>();
        for (Drone drone : fleet) {
            if (drone.isAvailable()) {
                availableDrones.add(drone);
            }
        }
        return availableDrones;
    }

    public List<Drone> getDronesForPackage(Package pkg, double distance) {
        TreeMap<Double, List<Drone>> dronesByCapacity = new TreeMap<>();
        List<Drone> drones = getAvailableDrones();
        for (Drone drone : drones) {
            if (!drone.canReach(distance)) continue;
            if (!drone.canCarry(pkg)) continue;
            if (!drone.satisfiesPackageRequirements(pkg)) continue;

            double remainingCapacity = drone.getMaximumPayload() - drone.getCurrentLoad();
            List<Drone> currentDrones = dronesByCapacity.getOrDefault(remainingCapacity, new ArrayList<>());
            currentDrones.add(drone);
            dronesByCapacity.put(remainingCapacity, currentDrones);
        }
        if (dronesByCapacity.isEmpty()) {
            return new ArrayList<>();
        }

        return dronesByCapacity.get(dronesByCapacity.firstKey());
    }

    public List<Drone> getSuitableDronesForPath(Set<PackageRequirement> requirements, double distance, double load) {
        List<Drone> suitableDrones = new ArrayList<>();
        for (Drone drone : getAvailableDrones()) {
            if (!drone.canReach(distance)) continue;
            if (!drone.canCarry(load)) continue;

            boolean allRequirementsSatisfied = true;
            for (PackageRequirement requirement : requirements) {
                if (!drone.requirementSatisfied(requirement)) {
                    allRequirementsSatisfied = false;
                    break;
                }
            }
            if (allRequirementsSatisfied) {
                suitableDrones.add(drone);
            }
        }

        return suitableDrones;
    }

    public void checkFleetMaintenance() {
        final LocalDate currentDate = LocalDate.now();

        fleet.removeIf(drone -> {
            final LocalDate lastMaintenance = drone.getLastMaintenance();
            long monthsBetween = ChronoUnit.MONTHS.between(lastMaintenance, currentDate);
            if (monthsBetween >= MAINTENANCE_THRESHOLD) {
                dronesUnderMaintenance.add(drone);
                drone.setAvailability(false);
                return true;
            }
            return false;
        });
    }

    public void performMaintenance() {
        final Iterator<Personnel> mechanicIterator = crew.stream()
                .filter(p -> p.isAvailable() && p.getCertification() == PersonnelCertification.MECHANIC)
                .iterator();
        final Map<Personnel, Drone> assignments = new HashMap<>();

        dronesUnderMaintenance.removeIf(drone -> {
                if (mechanicIterator.hasNext()) {
                    Personnel mechanic = mechanicIterator.next();
                    mechanic.assignToWork();
                    assignments.put(mechanic, drone);
                    fleet.add(drone);
                    drone.setAvailability(true);
                    return true;
                }
                return false;
        });

        assignments.keySet().forEach(Personnel::releaseFromWork);

        if (!dronesUnderMaintenance.isEmpty()) {
            System.out.println("Not all drones could be repaired. There are " + dronesUnderMaintenance.size() + " drones remaining.");
        }
    }

    public void displayFleet() {
        int droneCounter = 1;
        for (Drone drone : fleet) {
            switch (drone) {
                case CargoDrone d -> System.out.println(droneCounter + "# " + drone.getName() + " - range: " + d.getFlightRange() + " - max payload: " +
                        d.getMaximumPayload() + " - max speed: " + d.getMaximumSpeed() + " - availability: " + (d.isAvailable() ? " available" : "not available") +
                        " - last maintenance: " + d.getLastMaintenance() + (d.isRefrigerated() ? "" : " not") + " refrigerated");

                case HighSpeedDrone d -> System.out.println(droneCounter + "# " + drone.getName() + " - range: " + d.getFlightRange() + " - max payload: " +
                        d.getMaximumPayload() + " - max speed: " + d.getMaximumSpeed() + " - availability: " + (d.isAvailable() ? " available" : "not available") +
                        " - last maintenance: " + d.getLastMaintenance() + " - can handle express deliveries");

                case Drone d -> System.out.println(droneCounter + "# " + drone.getName() + " - range: " + d.getFlightRange() + " - max payload: " +
                        d.getMaximumPayload() + " - max speed: " + d.getMaximumSpeed() + " - availability: " + (d.isAvailable() ? "" : " not") +
                        " available - last maintenance: " + d.getLastMaintenance());
            }
            droneCounter++;
        }
    }
}
