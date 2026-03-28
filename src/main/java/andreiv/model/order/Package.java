package andreiv.model.order;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import andreiv.model.PackageRequirement;
import java.util.Collections;

public class Package {
    private final UUID id;
    private final double weight;
    private final double width;
    private final double length;
    private final double height;
    private final Set<PackageRequirement> requirements;

    public Package(double weight, double width, double length, double height) {
        this.id = UUID.randomUUID();
        this.weight = validateDimension(weight);
        this.width = validateDimension(width);
        this.length = validateDimension(length);
        this.height = validateDimension(height);
        this.requirements = Collections.emptySet();
    }

    public Package(double weight, double width, double length, double height,
                   String[] requirements) {
        this.id = UUID.randomUUID();
        this.weight = validateDimension(weight);
        this.width = validateDimension(width);
        this.length = validateDimension(length);
        this.height = validateDimension(height);
        this.requirements = assignRequirements(requirements);
    }

    private double validateDimension(double dimension) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Invalid value provided for the dimension field.");
        }
        return dimension;
    }

    private Set<PackageRequirement> assignRequirements(String[] requirements) {
        Set<PackageRequirement> assignableRequirements = new HashSet<>();
        for (String requirement : requirements) {
            if (PackageRequirement.validateRequirement(requirement) != null){
                assignableRequirements.add(PackageRequirement.validateRequirement(requirement));
            }
        }
        return assignableRequirements;
    }

    public UUID getId() {
        return id;
    }

    public double getWeight() {
        return weight;
    }

    public double getVolume() {
        return width * length * height;
    }

    public Set<PackageRequirement> getRequirements() {
        return requirements;
    }
}
