package com.edussafy.backend.health.service;

import com.edussafy.backend.health.dto.HealthResponse;
import com.edussafy.backend.health.dto.HealthResponse.HealthCheckItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private static final String UP = "UP";
    private static final String DOWN = "DOWN";

    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;
    private final Clock clock;

    @Autowired
    public HealthService(JdbcTemplate jdbcTemplate, Environment environment) {
        this(jdbcTemplate, environment, Clock.systemUTC());
    }

    HealthService(JdbcTemplate jdbcTemplate, Environment environment, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.environment = environment;
        this.clock = clock;
    }

    public HealthResponse getHealth() {
        List<HealthCheckItem> checks = new ArrayList<>();
        checks.add(databaseCheck());
        checks.add(tempStorageCheck());

        String status = checks.stream()
                .filter(HealthCheckItem::required)
                .allMatch(check -> UP.equals(check.status())) ? UP : DOWN;

        return new HealthResponse(
                status,
                OffsetDateTime.now(clock),
                "edussafy-backend",
                activeProfile(),
                List.copyOf(checks)
        );
    }

    private HealthCheckItem databaseCheck() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (Integer.valueOf(1).equals(result)) {
                return new HealthCheckItem("database", UP, true, "MySQL connectivity check passed.");
            }
            return new HealthCheckItem("database", DOWN, true, "Unexpected database probe result.");
        } catch (RuntimeException ex) {
            return new HealthCheckItem("database", DOWN, true, "Database probe failed: " + safeMessage(ex));
        }
    }

    private HealthCheckItem tempStorageCheck() {
        try {
            Path tempDir = Path.of(System.getProperty("java.io.tmpdir"));
            if (Files.isDirectory(tempDir) && Files.isWritable(tempDir)) {
                return new HealthCheckItem("temp-storage", UP, true, "Temporary attachment storage is writable.");
            }
            return new HealthCheckItem("temp-storage", DOWN, true, "Temporary attachment storage is not writable.");
        } catch (RuntimeException ex) {
            return new HealthCheckItem("temp-storage", DOWN, true, "Temporary storage probe failed: " + safeMessage(ex));
        }
    }

    private String activeProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return "default";
        }
        return String.join(",", Arrays.stream(activeProfiles).sorted().toList());
    }

    private String safeMessage(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null || message.isBlank() ? ex.getClass().getSimpleName() : message;
    }
}
