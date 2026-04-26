package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DeploymentSmokeScriptTest {

    private static final Path POWERSHELL_SMOKE_SCRIPT = Path.of("..", "scripts", "dev", "smoke.ps1");
    private static final Path POSIX_SMOKE_SCRIPT = Path.of("..", "scripts", "dev", "smoke.sh");
    private static final Path BOARD_SEED_SCRIPT = Path.of("..", "scripts", "mysql", "20-board-list-seed.sql");

    @Test
    void powershellSmokeScriptChecksReadinessThroughBackendAndNginx() throws IOException {
        String script = Files.readString(POWERSHELL_SMOKE_SCRIPT);

        assertThat(script).contains("Test-HttpEndpoint \"$BaseUrl/api/readiness\"");
        assertThat(script).contains("Test-HttpEndpoint \"$BackendUrl/api/readiness\"");
        assertThat(script).contains("Test-HttpEndpoint \"$BackendUrl/api/health\"");
    }

    @Test
    void posixSmokeScriptCoversProductionReadinessEndpoints() throws IOException {
        String script = Files.readString(POSIX_SMOKE_SCRIPT);

        assertThat(script).startsWith("#!/usr/bin/env bash");
        assertThat(script).contains("request GET \"$BASE_URL/api/readiness\"");
        assertThat(script).contains("request GET \"$BACKEND_URL/api/readiness\"");
        assertThat(script).contains("request POST \"$BACKEND_URL/api/auth/login\"");
        assertThat(script).contains("/ops/readiness");
    }

    @Test
    void posixSmokeScriptStoresSessionCookieForAuthenticatedSmokeRequests() throws IOException {
        String script = Files.readString(POSIX_SMOKE_SCRIPT);

        assertThat(script).contains("cookie_file=\"$(mktemp)\"");
        assertThat(script).contains("-b \"$cookie_file\" -c \"$cookie_file\"");
        assertThat(script).contains("request POST \"$BACKEND_URL/api/auth/login\"");
        assertThat(script).contains("request GET \"$BACKEND_URL/api/me\"");
    }

    @Test
    void boardSeedProvidesAllSmokeBoardGroups() throws IOException {
        String seed = Files.readString(BOARD_SEED_SCRIPT);

        assertThat(seed)
                .contains("('BOARD_GROUP', 'help'")
                .contains("('BOARD_GROUP', 'mentoring'")
                .contains("('BOARD_GROUP', 'external'")
                .doesNotContain("UNION ALL\n  UNION ALL");
    }

}
