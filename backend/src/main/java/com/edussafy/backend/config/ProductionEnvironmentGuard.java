package com.edussafy.backend.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
public class ProductionEnvironmentGuard implements ApplicationRunner {

    private static final List<String> REQUIRED_SECRET_PROPERTIES = List.of(
            "spring.datasource.url",
            "spring.datasource.username",
            "spring.datasource.password",
            "spring.rabbitmq.username",
            "spring.rabbitmq.password"
    );

    private final Environment environment;

    public ProductionEnvironmentGuard(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!environment.acceptsProfiles(Profiles.of("prod"))) {
            return;
        }

        List<String> failures = new ArrayList<>();
        for (String property : REQUIRED_SECRET_PROPERTIES) {
            String value = readProperty(property);
            if (isUnsafeSecret(value)) {
                failures.add(property);
            }
        }

        if (!environment.getProperty("server.servlet.session.cookie.secure", Boolean.class, false)) {
            failures.add("server.servlet.session.cookie.secure");
        }
        String sameSite = environment.getProperty("server.servlet.session.cookie.same-site", "");
        if (!"strict".equalsIgnoreCase(sameSite)) {
            failures.add("server.servlet.session.cookie.same-site");
        }
        if (environment.getProperty("edussafy.auth.password.allow-noop", Boolean.class, false)) {
            failures.add("edussafy.auth.password.allow-noop");
        }

        if (!failures.isEmpty()) {
            throw new IllegalStateException("Production profile is missing secure environment values: " + String.join(", ", failures));
        }
    }

    private String readProperty(String property) {
        try {
            return environment.getProperty(property);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static boolean isUnsafeSecret(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("${")
                || normalized.contains("change-me")
                || normalized.contains("changeme")
                || normalized.contains("replace-me")
                || normalized.equals("todo")
                || normalized.equals("example");
    }
}
