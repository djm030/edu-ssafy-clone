package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CiWorkflowHardeningTest {

    private static final Path CI_WORKFLOW = Path.of("..", ".github", "workflows", "ci.yml");
    private static final Path CORE_E2E_SPEC = Path.of("..", "frontend", "e2e", "core-flows.spec.ts");
    private static final Path CI_SIGNOFF_SCRIPT = Path.of("..", "scripts", "dev", "ci-signoff.sh");

    @Test
    void ciRunsProductionHardeningSmokeAndDocumentationGuards() throws IOException {
        String workflow = Files.readString(CI_WORKFLOW);

        assertThat(workflow)
                .contains("Set up Java 21")
                .contains("Set up Node 22")
                .contains("scripts/dev/scan-secrets.sh")
                .contains("docker compose -f compose.yml --profile app config")
                .contains("bash -n scripts/dev/smoke.sh")
                .contains("bash -n scripts/dev/smoke-routes.sh")
                .contains("bash -n scripts/dev/verify-restdocs.sh")
                .contains("bash -n scripts/dev/verify-openapi.sh")
                .contains("bash -n scripts/dev/smoke-observability.sh")
                .contains("SKIP_HTTP=true scripts/dev/smoke.sh")
                .contains("SKIP_HTTP=true scripts/dev/smoke-routes.sh")
                .contains("SKIP_HTTP=true scripts/dev/smoke-observability.sh")
                .contains("RUN_TESTS=false scripts/dev/verify-restdocs.sh")
                .contains("scripts/dev/verify-openapi.sh")
                .contains("npm --prefix frontend run lint")
                .contains("npm --prefix frontend run build")
                .contains("npx playwright install --with-deps chromium")
                .contains("npm --prefix frontend run e2e -- --project=chromium e2e/core-flows.spec.ts")
                .contains("Generate hosted CI sign-off checklist")
                .contains("scripts/dev/ci-signoff.sh")
                .contains("actions/upload-artifact@v4")
                .contains("ci-signoff-${{ github.run_id }}-${{ github.run_attempt }}")
                .contains("build/reports/ci-signoff.md")
                .contains("build/reports/observability-smoke.jsonl")
                .contains("if: always()");
    }

    @Test
    void ciSignoffScriptRecordsHostedEvidenceWithoutSecrets() throws IOException {
        String script = Files.readString(CI_SIGNOFF_SCRIPT);

        assertThat(script)
                .startsWith("#!/usr/bin/env bash")
                .contains("CI_SIGNOFF_OUT")
                .contains("GITHUB_STEP_SUMMARY")
                .contains("Hosted CI Sign-off Checklist")
                .contains("Docker Compose base/app/observability configs")
                .contains("Backend Maven test suite")
                .contains("Spring REST Docs snippet verifier")
                .contains("Browser E2E core flows")
                .contains("Browser visual baseline")
                .contains("build/reports/observability-smoke.jsonl")
                .doesNotContain("@naver.com")
                .doesNotContain("djm" + "062954");
    }

    @Test
    void ciSignoffScriptCanRunLocallyForArtifactShape() throws IOException, InterruptedException {
        Path signoff = Files.createTempFile("ci-signoff", ".md");
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "../scripts/dev/ci-signoff.sh");
        Map<String, String> environment = processBuilder.environment();
        environment.put("CI_SIGNOFF_OUT", signoff.toString());
        environment.put("CI_RUN_URL", "https://example.invalid/actions/runs/123");
        environment.put("GITHUB_REPOSITORY", "ssafy/full-clone");
        environment.put("GITHUB_REF_NAME", "main");
        environment.put("GITHUB_SHA", "abc123");
        environment.put("GITHUB_RUN_ATTEMPT", "1");
        Process process = processBuilder.start();

        boolean finished = process.waitFor(Duration.ofSeconds(10).toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        String body = Files.readString(signoff);

        assertThat(finished).as(stderr).isTrue();
        assertThat(process.exitValue()).as(stderr).isZero();
        assertThat(stdout).contains("[ci-signoff] wrote");
        assertThat(body)
                .contains("# Hosted CI Sign-off Checklist")
                .contains("https://example.invalid/actions/runs/123")
                .contains("ssafy/full-clone")
                .contains("abc123")
                .contains("- [x] Backend Maven test suite completed.")
                .contains("seeded demo data only")
                .contains("no production sign-off");
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
                .contains("/mycampus/elearning")
                .contains("/mycampus/bookmarks")
                .contains("/mycampus/documents")
                .contains("/mycampus/pledges")
                .contains("/learning/required-studies")
                .contains("/learning/live")
                .contains("/learning/replays/my")
                .contains("/community/classmates")
                .contains("알림 권한 없음")
                .contains("loginAsDemoCoach")
                .contains("알림을 보냈습니다.")
                .contains("서류 제출이 완료되었습니다.")
                .contains("서약 동의가 저장되었습니다.")
                .contains("without real EduSSAFY credentials")
                .doesNotContain("@naver.com")
                .doesNotContain("djm" + "062954");
    }
}
