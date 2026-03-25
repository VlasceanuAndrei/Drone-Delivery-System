package andreiv.model;

import java.util.EnumSet;

public enum PackageRequirement {
    REFRIGERATED,
    EXPRESS_DELIVERY,
    FRAGILE,
    HAZARDOUS;

    private static final EnumSet<PackageRequirement> VALUES = EnumSet.allOf(PackageRequirement.class);

    public static PackageRequirement validateRequirement(String requirement) {
        try {
            return PackageRequirement.valueOf(requirement.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
