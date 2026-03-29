package andreiv.model.hub;

import java.util.*;
import andreiv.model.drone.*;
import andreiv.model.order.*;
import andreiv.model.order.Package;
import andreiv.model.personnel.*;
import andreiv.model.PackageRequirement;

public class DroneHub {
    private String name;
    private final List<Drone> fleet;
    private final List<Package> packages;
    private final List<Personnel> crew;
    private Address address;

    public DroneHub(String name, List<Drone> fleet, List<andreiv.model.order.Package> packages,
                    List<Personnel> crew, Address address) {
        this.name = name;
        this.fleet = fleet;
        this.packages = packages;
        this.crew = crew;
        this.address = address;
    }

    public DroneHub(String name, Address address) {
        this.name = name;
        this.address = address;
        this.fleet = new ArrayList<>();
        this.packages = new ArrayList<>();
        this.crew = new ArrayList<>();
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
        for (Package pack : packages) {
            if (pack.getRequirements().contains(requirement)) {
                filteredPackages.add(pack);
            }
        }
        return filteredPackages;
    }

    public Address getAddress() {
        return address;
    }
}
