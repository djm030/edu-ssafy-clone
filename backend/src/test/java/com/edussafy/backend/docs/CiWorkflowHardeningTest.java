package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CiWorkflowHardeningTest {

    private static final Path CI_WORKFLOW = Path.of("..", ".github", "workflows", "ci.yml");
    private static final Path CORE_E2E_SPEC = Path.of("..", "frontend", "e2e", "core-flows.spec.ts");

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
                .contains("bash -n scripts/dev/smoke-observability.sh")
                .contains("SKIP_HTTP=true scripts/dev/smoke.sh")
                .contains("SKIP_HTTP=true scripts/dev/smoke-routes.sh")
                .contains("SKIP_HTTP=true scripts/dev/smoke-observability.sh")
                .contains("RUN_TESTS=false scripts/dev/verify-restdocs.sh")
                .contains("npm --prefix frontend run lint")
                .contains("npm --prefix frontend run build")
                .contains("npx playwright install --with-deps chromium")
                .contains("npm --prefix frontend run e2e -- --project=chromium e2e/core-flows.spec.ts");
    }

    @Test
    void browserE2eCoversCoreMutationsWithoutRealEduSsafyCredentials() throws IOException {
        String spec = Files.readString(CORE_E2E_SPEC);

        assertThat(spec)
                .contains("/mycampus/attendance/appeals/new")
                .contains("/community/free/write")
                .contains("/community/free/201")
                .contains("/survey/1/respond")
                .contains("/help/qna/new")
                .contains("/quest/1/submit")
                .contains("without real EduSSAFY credentials")
                .doesNotContain("@naver.com")
                .doesNotContain("djm062954");
    }
}
