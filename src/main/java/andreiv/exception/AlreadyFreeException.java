package andreiv.exception;

import java.util.UUID;

public class AlreadyFreeException extends RuntimeException {
    public AlreadyFreeException(UUID id, String fullName) {
        super(fullName + " (ID:" + id + ") isn't currently working on a task, so they cannot be released.");
    }
}
