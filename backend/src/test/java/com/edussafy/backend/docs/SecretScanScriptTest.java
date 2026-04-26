package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SecretScanScriptTest {

    private static final Path SCRIPT = Path.of("..", "scripts", "dev", "scan-secrets.sh");

    @Test
    void secretScanBlocksRealAccountCookiesAndPrivateKeysWithoutRejectingPlaceholders() throws IOException {
        String script = Files.readString(SCRIPT);

        assertThat(script).startsWith("#!/usr/bin/env bash");
        assertThat(script).contains("djm030");
        assertThat(script).contains("djm062954");
        assertThat(script).contains("JSESSIONID=");
        assertThat(script).contains("PRIVATE KEY");
        assertThat(script).contains("AWS_SECRET_ACCESS_KEY=");
        assertThat(script).contains("potential committed credential detected");
        assertThat(script).doesNotContain("change-me-root-password|");
    }
}
