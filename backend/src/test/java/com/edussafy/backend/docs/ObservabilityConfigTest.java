package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ObservabilityConfigTest {

    @Test
    void actuatorMetricsAndPrometheusEndpointAreExposed() throws IOException {
        String application = new ClassPathResource("application.yml").getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        String pom = Files.readString(Path.of("pom.xml"));

        assertThat(application).contains("include: health,info,metrics,prometheus");
        assertThat(application).contains("application: edussafy-backend");
        assertThat(pom).contains("micrometer-registry-prometheus");
    }

    @Test
    void observabilityComposeDeclaresMetricsStackWithHealthchecks() throws IOException {
        String compose = Files.readString(Path.of("..", "compose.observability.yml"));

        assertThat(compose).contains("prometheus:");
        assertThat(compose).contains("grafana:");
        assertThat(compose).contains("PROMETHEUS_PORT");
        assertThat(compose).contains("GRAFANA_PORT");
        assertThat(compose).contains("/-/healthy");
        assertThat(compose).contains("/api/health");
    }

    @Test
    void prometheusScrapesBackendActuatorEndpoint() throws IOException {
        String prometheus = Files.readString(Path.of("..", "infra", "prometheus", "prometheus.yml"));

        assertThat(prometheus).contains("job_name: ssafy-backend");
        assertThat(prometheus).contains("metrics_path: /actuator/prometheus");
        assertThat(prometheus).contains("targets: ['backend:8080']");
    }

    @Test
    void grafanaDashboardProvisioningTargetsPrometheusDatasource() throws IOException {
        String datasource = Files.readString(Path.of("..", "infra", "grafana", "provisioning", "datasources", "prometheus.yml"));
        String dashboardProvider = Files.readString(Path.of("..", "infra", "grafana", "provisioning", "dashboards", "default.yml"));
        String dashboard = Files.readString(Path.of("..", "infra", "grafana", "dashboards", "ssafy-clone-overview.json"));

        assertThat(datasource).contains("type: prometheus");
        assertThat(datasource).contains("url: http://prometheus:9090");
        assertThat(dashboardProvider).contains("path: /var/lib/grafana/dashboards");
        assertThat(dashboard).contains("SSAFY Clone Overview");
        assertThat(dashboard).contains("http_server_requests_seconds_count");
        assertThat(dashboard).contains("jvm_memory_used_bytes");
    }
    @Test
    void observabilitySmokeScriptChecksBackendPrometheusAndGrafanaEndpoints() throws IOException {
        String script = Files.readString(Path.of("..", "scripts", "dev", "smoke-observability.sh"));

        assertThat(script).startsWith("#!/usr/bin/env bash");
        assertThat(script).contains("$BACKEND_URL/actuator/health");
        assertThat(script).contains("$BACKEND_URL/actuator/metrics");
        assertThat(script).contains("$BACKEND_URL/actuator/prometheus");
        assertThat(script).contains("$PROMETHEUS_URL/-/healthy");
        assertThat(script).contains("$PROMETHEUS_URL/api/v1/targets?state=active");
        assertThat(script).contains("$GRAFANA_URL/api/health");
        assertThat(script).contains("SKIP_HTTP=true");
    }

    @Test
    void opsReadinessScreenRunsActuatorMetricsAndPrometheusChecks() throws IOException {
        String readinessApi = Files.readString(Path.of("..", "frontend", "src", "api", "readiness.ts"));
        String readinessPage = Files.readString(Path.of("..", "frontend", "src", "pages", "OpsReadinessPage.tsx"));
        String coreFlows = Files.readString(Path.of("..", "frontend", "e2e", "core-flows.spec.ts"));

        assertThat(readinessApi)
                .contains("id: 'actuator-metrics'")
                .contains("target: '/actuator/metrics'")
                .contains("id: 'prometheus-metrics'")
                .contains("target: '/actuator/prometheus'");
        assertThat(readinessPage).contains("actuator metrics");
        assertThat(coreFlows)
                .contains("Actuator metrics")
                .contains("Prometheus metrics")
                .contains("READY");
    }

}
