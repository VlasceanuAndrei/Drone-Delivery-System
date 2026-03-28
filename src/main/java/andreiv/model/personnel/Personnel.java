package andreiv.model.personnel;

import java.util.UUID;
import andreiv.model.PersonnelCertification;

public class Personnel {
    private UUID id;
    private String fullName;
    private PersonnelCertification certification;

    public Personnel(UUID id, String fullName, String certification) {
        this.id = UUID.randomUUID();
        this.fullName = fullName;
        this.certification = PersonnelCertification.validateCertification(certification);
    }

    public String getFullName() {
        return fullName;
    }

    public PersonnelCertification getCertification() {
        return certification;
    }

}
