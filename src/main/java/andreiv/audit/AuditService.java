package andreiv.audit;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.nio.charset.StandardCharsets;

public final class AuditService {
    private static final Path DESTINATION = Path.of(System.getProperty("user.dir"), "audit.csv");

    private AuditService() {}

    public static void audit(String actionName) {
        String line = actionName + ", " + Instant.now();

        try {
            Files.writeString(
                    DESTINATION,
                    line + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Audit append failed: " + e.getMessage());
        }
    }
}
