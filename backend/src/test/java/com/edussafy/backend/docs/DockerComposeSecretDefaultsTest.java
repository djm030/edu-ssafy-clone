package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DockerComposeSecretDefaultsTest {

    @Test
    void composeFilesUsePlaceholdersInsteadOfReusableDevSecrets() throws IOException {
        String compose = Files.readString(Path.of("..", "compose.yml"));
        String mysqlOnlyCompose = Files.readString(Path.of("..", "compose.mysql.yml"));

        assertThat(compose).doesNotContain("ssafy_dev_root_password");
        assertThat(compose).doesNotContain("ssafy_dev_password");
        assertThat(mysqlOnlyCompose).doesNotContain("ssafy_dev_root_password");
        assertThat(compose).contains("change-me-root-password");
        assertThat(compose).contains("change-me-rabbit-password");
        assertThat(mysqlOnlyCompose).contains("change-me-root-password");
    }
    @Test
    void backendHealthcheckUsesDependencyAwareReadinessEndpoint() throws IOException {
        String compose = Files.readString(Path.of("..", "compose.yml"));

        assertThat(compose).contains("curl -fsS http://localhost:8080/api/readiness >/dev/null");
        assertThat(compose).doesNotContain("curl -fsS http://localhost:8080/actuator/health >/dev/null");
    }

}
