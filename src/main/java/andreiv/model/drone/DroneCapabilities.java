package andreiv.model.drone;

import andreiv.model.PackageRequirement;
import andreiv.model.order.Package;

public interface DroneCapabilities {
    boolean canHandleRefrigeratedPackage();
    boolean canHandleExpressDelivery();
    boolean canHandleFragilePackage();
    boolean canHandleHazardousPackage();

    default boolean requirementSatisfied(PackageRequirement r) {
        return switch (r) {
            case REFRIGERATED -> canHandleRefrigeratedPackage();
            case EXPRESS_DELIVERY -> canHandleExpressDelivery();
            case FRAGILE -> canHandleFragilePackage();
            case HAZARDOUS -> canHandleHazardousPackage();
            default -> true;
        };
    }

    default boolean satisfiesPackageRequirements(Package pkg) {
        for (PackageRequirement r : pkg.getRequirements()) {
            if (!requirementSatisfied(r)) {
                return false;
            }
        }
        return true;
    }
}
