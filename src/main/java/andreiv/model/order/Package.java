package andreiv.model.order;

import java.util.UUID;
import java.util.Set;
import andreiv.model.PackageRequirement;

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
        this.requirements = null;
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
        Set<PackageRequirement> assignableRequirements = null;
        for (String requirement : requirements) {
            if (PackageRequirement.validateRequirement(requirement) != null){
                assignableRequirements.add(PackageRequirement.validateRequirement(requirement));
            }
        }
        return assignableRequirements;
    }

    public double getVolume() {
        return width * length * height;
    }
}
