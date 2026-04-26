package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class EnvironmentExampleConfigTest {

    private static final Path ENV_EXAMPLE = Path.of("..", ".env.example");

    @Test
    void envExampleUsesPlaceholdersInsteadOfReusableDevSecrets() throws IOException {
        String env = Files.readString(ENV_EXAMPLE);

        assertThat(env).contains("MYSQL_ROOT_PASSWORD=change-me-root-password");
        assertThat(env).contains("RABBITMQ_DEFAULT_PASS=change-me-rabbit-password");
        assertThat(env).doesNotContain("ssafy_dev_root_password");
        assertThat(env).doesNotContain("ssafy_dev_password");
    }

    @Test
    void envExampleDocumentsProductionCookieAndSecretChecklist() throws IOException {
        String env = Files.readString(ENV_EXAMPLE);

        assertThat(env).contains("SPRING_PROFILES_ACTIVE=docker");
        assertThat(env).contains("EDUSSAFY_AUTH_ALLOW_NOOP_PASSWORDS=false");
        assertThat(env).contains("SERVER_SERVLET_SESSION_COOKIE_SECURE=false");
        assertThat(env).contains("SERVER_SERVLET_SESSION_COOKIE_SAME_SITE=lax");
        assertThat(env).contains("SPRING_PROFILES_ACTIVE=prod");
        assertThat(env).contains("SERVER_SERVLET_SESSION_COOKIE_SECURE=true");
        assertThat(env).contains("SPRING_DATASOURCE_URL");
        assertThat(env).contains("SPRING_RABBITMQ_PASSWORD");
    }
}
