package andreiv.exception;

import java.util.UUID;

public class AlreadyAssignedException extends RuntimeException {
    public AlreadyAssignedException(UUID id, String fullName) {
        super(fullName + " (ID:" + id + ") is already assigned to a task. Wait for the task to finish in order to assign another one.");
    }
}
