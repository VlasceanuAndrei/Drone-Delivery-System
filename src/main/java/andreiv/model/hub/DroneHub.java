package andreiv.model.hub;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.time.LocalDate;

import andreiv.model.drone.*;
import andreiv.model.order.*;
import andreiv.model.order.Package;
import andreiv.model.personnel.*;
import andreiv.model.PackageRequirement;
import andreiv.model.PersonnelCertification;

public class DroneHub {
    private final String name;
    private final List<Drone> fleet;
    private final List<Package> packages;
    private final List<Personnel> crew;
    private final Address address;
    private final List<Drone> dronesUnderMaintenance;
    private final static int MAINTENANCE_THRESHOLD = 18;

    public DroneHub(String name, Address address) {
        this(name, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), address);
    }

    public DroneHub(String name, List<Drone> fleet, List<andreiv.model.order.Package> packages,
                    List<Personnel> crew, Address address) {
        this.name = name;
        this.fleet = fleet;
        this.packages = packages;
        this.crew = crew;
        this.address = address;
        this.dronesUnderMaintenance = new ArrayList<>();
    }

    public void displayFleet() {
        int count = 1;
        for (Drone drone : fleet) {
            System.out.println(count + ". " + drone.getName() + " - " + drone.getFlightRange() +
                    " km range - " + drone.getMaximumPayload() + " maximum payload");
        }
    }

    public void addDrone(Drone drone) {
        fleet.add(drone);
    }

    public void removeDrone(Drone drone) {
        fleet.remove(drone);
    }

    public void addPackage(Package pack) {
        packages.add(pack);
    }

    public void removePackage(Package pack) {
        packages.remove(pack);
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
            if (drone.getAvailability()) {
                availableDrones.add(drone);
            }
        }
        return availableDrones;
    }

    public List<Package> getPackagesByCategory(PackageRequirement requirement) {
        List<Package> filteredPackages = new ArrayList<>();
        for (Package pkg : packages) {
            if (pkg.getRequirements().contains(requirement)) {
                filteredPackages.add(pkg);
            }
        }
        return filteredPackages;
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

    public String getName() { return name; }

    public Address getAddress() {
        return address;
    }
}
