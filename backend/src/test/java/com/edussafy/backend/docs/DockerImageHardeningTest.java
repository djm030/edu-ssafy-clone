package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DockerImageHardeningTest {

    private static final Path BACKEND_DOCKERFILE = Path.of("Dockerfile");

    @Test
    void backendRuntimeImageDropsRootPrivileges() throws IOException {
        String dockerfile = Files.readString(BACKEND_DOCKERFILE);

        assertThat(dockerfile).contains("useradd --system");
        assertThat(dockerfile).contains("chown -R app:app /app");
        assertThat(dockerfile).contains("USER app");
        assertThat(dockerfile).doesNotContain("USER root");
    }
}
