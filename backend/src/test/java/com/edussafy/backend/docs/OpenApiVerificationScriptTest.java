package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OpenApiVerificationScriptTest {

    private static final Path OPENAPI_SCRIPT = Path.of("..", "scripts", "dev", "verify-openapi.sh");
    private static final Path CI_WORKFLOW = Path.of("..", ".github", "workflows", "ci.yml");

    @Test
    void posixOpenApiVerifierChecksGeneratedSnapshotAndCatalog() throws IOException {
        String script = Files.readString(OPENAPI_SCRIPT);

        assertThat(script)
                .startsWith("#!/usr/bin/env bash")
                .contains("docs/openapi.json")
                .contains("backend/src/test/resources/api-docs-endpoints.tsv")
                .contains("/api/auth/login")
                .contains("/api/external-services/{code}/access-log")
                .contains("Test-AuthJsonShape")
                .contains("docs/openapi.yaml")
                .contains("cataloged endpoint missing");
    }

    @Test
    void ciUsesPosixOpenApiVerifierInsteadOfStaleYamlGreps() throws IOException {
        String workflow = Files.readString(CI_WORKFLOW);

        assertThat(workflow)
                .contains("bash -n scripts/dev/verify-openapi.sh")
                .contains("scripts/dev/verify-openapi.sh")
                .doesNotContain("docs/openapi.yaml");
    }

    @Test
    void posixOpenApiVerifierRunsAgainstCommittedSnapshot() throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "../scripts/dev/verify-openapi.sh");
        Map<String, String> environment = processBuilder.environment();
        environment.put("SPEC_PATH", Path.of("..", "docs", "openapi.json").toAbsolutePath().toString());
        environment.put("SMOKE_PATH", Path.of("..", "scripts", "dev", "smoke.ps1").toAbsolutePath().toString());
        environment.put("CATALOG_PATH", Path.of("src", "test", "resources", "api-docs-endpoints.tsv").toAbsolutePath().toString());
        Process process = processBuilder.start();
        boolean finished = process.waitFor(Duration.ofSeconds(10).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);

        assertThat(finished).as(stderr).isTrue();
        assertThat(process.exitValue()).as(stderr).isZero();
        assertThat(stdout).contains("verified generated Swagger/OpenAPI snapshot markers");
    }
}
