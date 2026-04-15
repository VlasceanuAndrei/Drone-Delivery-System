package andreiv.model.personnel;

import java.util.UUID;
import andreiv.model.PersonnelCertification;
import andreiv.exception.AlreadyAssignedException;
import andreiv.exception.AlreadyFreeException;

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

    public UUID getId() {
        return id;
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
            throw new AlreadyAssignedException(id, fullName);
        }
        isAvailable = false;
    }

    public void releaseFromWork() {
        if (isAvailable) {
            throw new AlreadyFreeException(id, fullName);
        }
        isAvailable = true;
    }
}
