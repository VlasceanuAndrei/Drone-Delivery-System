package andreiv.model;


public enum PersonnelCertification {
    NONE,
    MECHANIC,
    OPERATOR,
    COMMANDER;

    public static PersonnelCertification validateCertification(String certification) {
        if (certification == null) {
            return NONE;
        }
        try {
            return PersonnelCertification.valueOf(certification.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
