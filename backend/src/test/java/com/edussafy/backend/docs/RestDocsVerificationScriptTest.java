package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RestDocsVerificationScriptTest {

    private static final Path RESTDOCS_SCRIPT = Path.of("..", "scripts", "dev", "verify-restdocs.sh");

    @Test
    void posixRestDocsVerificationRunsDocumentationTestsAndChecksRequiredSnippets() throws IOException {
        String script = Files.readString(RESTDOCS_SCRIPT);

        assertThat(script).startsWith("#!/usr/bin/env bash");
        assertThat(script).contains("ApiRestDocsTest,AuthRestDocsTest,SurveyRestDocsTest");
        assertThat(script).contains("maven:3.9.9-eclipse-temurin-21");
        assertThat(script).contains("health-check");
        assertThat(script).contains("readiness-check");
        assertThat(script).contains("auth-access-policy");
        assertThat(script).contains("survey-create");
        assertThat(script).contains("survey-update");
        assertThat(script).contains("survey-delete");
        assertThat(script).contains("response-fields.adoc");
        assertThat(script).contains("request-fields.adoc");
    }
}
