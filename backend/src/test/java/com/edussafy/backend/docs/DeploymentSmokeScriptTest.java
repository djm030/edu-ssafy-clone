package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DeploymentSmokeScriptTest {

    private static final Path SMOKE_SCRIPT = Path.of("..", "scripts", "dev", "smoke.ps1");

    @Test
    void smokeScriptChecksReadinessThroughBackendAndNginx() throws IOException {
        String script = Files.readString(SMOKE_SCRIPT);

        assertThat(script).contains("Test-HttpEndpoint \"$BaseUrl/api/readiness\"");
        assertThat(script).contains("Test-HttpEndpoint \"$BackendUrl/api/readiness\"");
        assertThat(script).contains("Test-HttpEndpoint \"$BackendUrl/api/health\"");
    }
}
