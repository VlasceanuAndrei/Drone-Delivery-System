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
        this(UUID.randomUUID(), fullName, certification, true);
    }

    public Personnel(UUID id, String fullName, String certification) {
        this(id, fullName, certification, true);
    }

    public Personnel(UUID id, String fullName, String certification, boolean isAvailable) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.fullName = fullName;
        this.certification = PersonnelCertification.validateCertification(certification);
        this.isAvailable = isAvailable;
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
