package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ProductionProfileConfigTest {

    @Test
    void prodProfileRequiresSecretEnvironmentVariables() throws IOException {
        String config = readProdConfig();

        assertThat(config).contains("password: ${SPRING_DATASOURCE_PASSWORD}");
        assertThat(config).contains("username: ${SPRING_RABBITMQ_USERNAME}");
        assertThat(config).contains("password: ${SPRING_RABBITMQ_PASSWORD}");
        assertThat(config).doesNotContain("SPRING_DATASOURCE_PASSWORD:");
        assertThat(config).doesNotContain("SPRING_RABBITMQ_PASSWORD:");
    }

    @Test
    void prodProfileDefaultsToSecureStrictSessionCookiesAndDisablesNoopPasswords() throws IOException {
        String config = readProdConfig();

        assertThat(config).contains("secure: ${SERVER_SERVLET_SESSION_COOKIE_SECURE:true}");
        assertThat(config).contains("same-site: ${SERVER_SERVLET_SESSION_COOKIE_SAME_SITE:strict}");
        assertThat(config).contains("allow-noop: false");
    }

    private static String readProdConfig() throws IOException {
        ClassPathResource resource = new ClassPathResource("application-prod.yml");
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
}
