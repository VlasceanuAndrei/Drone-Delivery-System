package andreiv.model;

public enum PackageRequirement {
    NONE,
    REFRIGERATED,
    EXPRESS_DELIVERY,
    FRAGILE,
    HAZARDOUS;

    public static PackageRequirement validateRequirement(String requirement) {
        if (requirement == null) {
            return NONE;
        }
        try {
            return PackageRequirement.valueOf(requirement.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
