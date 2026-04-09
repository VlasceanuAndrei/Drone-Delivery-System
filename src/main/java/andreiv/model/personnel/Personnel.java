package andreiv.model.personnel;

import java.util.UUID;
import andreiv.model.PersonnelCertification;

public class Personnel {
    private final UUID id;
    private final String fullName;
    private final PersonnelCertification certification;
    private boolean isAvailable;

    public Personnel(String fullName, String certification) {
        this.id = UUID.randomUUID();
        this.fullName = fullName;
        this.certification = PersonnelCertification.validateCertification(certification);
        this.isAvailable = true;
    }

    public String getFullName() {
        return fullName;
    }

    public PersonnelCertification getCertification() {
        return certification;
    }

    public boolean isAvailable() { return isAvailable; }

    public void assignToWork() {
        if (!isAvailable) {
            throw new IllegalStateException(fullName + " is already assigned to a task.");
        }
        isAvailable = false;
    }

    public void releaseFromWork() {
        if (isAvailable) {
            throw new IllegalStateException(fullName + " is not currently assigned.");
        }
        isAvailable = true;
    }
}
