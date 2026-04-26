package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.edussafy.backend.config.ProductionEnvironmentGuard;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class ProductionEnvironmentGuardTest {

    @Test
    void nonProdProfileDoesNotRequireProductionSecrets() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("docker");
        ProductionEnvironmentGuard guard = new ProductionEnvironmentGuard(environment);

        assertThatCode(() -> guard.run(null)).doesNotThrowAnyException();
    }

    @Test
    void prodProfileFailsFastWhenRequiredSecretsAreMissing() {
        MockEnvironment environment = prodEnvironment()
                .withProperty("server.servlet.session.cookie.secure", "true")
                .withProperty("server.servlet.session.cookie.same-site", "strict")
                .withProperty("edussafy.auth.password.allow-noop", "false");

        ProductionEnvironmentGuard guard = new ProductionEnvironmentGuard(environment);

        assertThatThrownBy(() -> guard.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spring.datasource.url")
                .hasMessageContaining("spring.rabbitmq.password");
    }

    @Test
    void prodProfileRejectsPlaceholderSecretsAndInsecureSessionSettings() {
        MockEnvironment environment = prodEnvironment()
                .withProperty("spring.datasource.url", "jdbc:mysql://mysql:3306/edussafy")
                .withProperty("spring.datasource.username", "edussafy")
                .withProperty("spring.datasource.password", "change-me-password")
                .withProperty("spring.rabbitmq.username", "edussafy")
                .withProperty("spring.rabbitmq.password", "${SPRING_RABBITMQ_PASSWORD}")
                .withProperty("server.servlet.session.cookie.secure", "false")
                .withProperty("server.servlet.session.cookie.same-site", "lax")
                .withProperty("edussafy.auth.password.allow-noop", "true");

        ProductionEnvironmentGuard guard = new ProductionEnvironmentGuard(environment);

        assertThatThrownBy(() -> guard.run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spring.datasource.password")
                .hasMessageContaining("spring.rabbitmq.password")
                .hasMessageContaining("server.servlet.session.cookie.secure")
                .hasMessageContaining("server.servlet.session.cookie.same-site")
                .hasMessageContaining("edussafy.auth.password.allow-noop");
    }

    @Test
    void prodProfileAllowsExplicitSecretsWithSecureStrictCookies() {
        MockEnvironment environment = prodEnvironment()
                .withProperty("spring.datasource.url", "jdbc:mysql://mysql:3306/edussafy")
                .withProperty("spring.datasource.username", "edussafy_prod")
                .withProperty("spring.datasource.password", "s3cure-db-value")
                .withProperty("spring.rabbitmq.username", "edussafy_mq")
                .withProperty("spring.rabbitmq.password", "s3cure-mq-value")
                .withProperty("server.servlet.session.cookie.secure", "true")
                .withProperty("server.servlet.session.cookie.same-site", "strict")
                .withProperty("edussafy.auth.password.allow-noop", "false");

        ProductionEnvironmentGuard guard = new ProductionEnvironmentGuard(environment);

        assertThatCode(() -> guard.run(null)).doesNotThrowAnyException();
    }

    private static MockEnvironment prodEnvironment() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        return environment;
    }
}
