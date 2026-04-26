package com.edussafy.backend.health.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.lang.reflect.Constructor;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

class HealthServiceTest {

    private final JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
    private final Environment environment = Mockito.mock(Environment.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-04-26T03:30:00Z"), ZoneOffset.UTC);

    @Test
    void returnsUpWhenRequiredDependenciesAreHealthy() {
        given(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).willReturn(1);
        given(environment.getActiveProfiles()).willReturn(new String[]{"prod"});

        HealthService healthService = new HealthService(jdbcTemplate, environment, clock);

        var response = healthService.getHealth();

        assertThat(response.status()).isEqualTo("UP");
        assertThat(response.profile()).isEqualTo("prod");
        assertThat(response.service()).isEqualTo("edussafy-backend");
        assertThat(response.checks()).extracting("name").contains("database", "temp-storage");
        assertThat(response.checks()).allMatch(check -> "UP".equals(check.status()));
    }

    @Test
    void returnsDownWhenDatabaseProbeFails() {
        given(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).willThrow(new IllegalStateException("db unavailable"));
        given(environment.getActiveProfiles()).willReturn(new String[]{});

        HealthService healthService = new HealthService(jdbcTemplate, environment, clock);

        var response = healthService.getHealth();

        assertThat(response.status()).isEqualTo("DOWN");
        assertThat(response.profile()).isEqualTo("default");
        assertThat(response.checks())
                .filteredOn(check -> "database".equals(check.name()))
                .singleElement()
                .satisfies(check -> {
                    assertThat(check.status()).isEqualTo("DOWN");
                    assertThat(check.message()).contains("db unavailable");
                });
    }

    @Test
    void publicConstructorIsSelectedForSpringInjection() throws NoSuchMethodException {
        Constructor<HealthService> constructor = HealthService.class.getConstructor(JdbcTemplate.class, Environment.class);

        assertThat(constructor.getAnnotation(Autowired.class)).isNotNull();
    }
}
