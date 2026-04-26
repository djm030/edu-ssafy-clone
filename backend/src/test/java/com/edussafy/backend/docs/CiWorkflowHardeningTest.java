package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CiWorkflowHardeningTest {

    private static final Path CI_WORKFLOW = Path.of("..", ".github", "workflows", "ci.yml");

    @Test
    void ciRunsProductionHardeningSmokeAndDocumentationGuards() throws IOException {
        String workflow = Files.readString(CI_WORKFLOW);

        assertThat(workflow)
                .contains("Set up Java 21")
                .contains("Set up Node 22")
                .contains("docker compose -f compose.yml --profile app config")
                .contains("bash -n scripts/dev/smoke.sh")
                .contains("bash -n scripts/dev/smoke-routes.sh")
                .contains("bash -n scripts/dev/verify-restdocs.sh")
                .contains("SKIP_HTTP=true scripts/dev/smoke.sh")
                .contains("SKIP_HTTP=true scripts/dev/smoke-routes.sh")
                .contains("RUN_TESTS=false scripts/dev/verify-restdocs.sh")
                .contains("npm --prefix frontend run lint")
                .contains("npm --prefix frontend run build");
    }
}
