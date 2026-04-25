package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class NginxReverseProxyConfigTest {

    private static final Path NGINX_CONF = Path.of("..", "infra", "nginx", "conf.d", "default.conf");

    @Test
    void nginxProxiesApiAndActuatorToBackendWithForwardedHeaders() throws IOException {
        String config = Files.readString(NGINX_CONF);

        assertThat(config).contains("location /api/");
        assertThat(config).contains("location /actuator/");
        assertThat(config).contains("proxy_pass http://ssafy_backend;");
        assertThat(config).contains("proxy_set_header X-Real-IP $remote_addr;");
        assertThat(config).contains("proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;");
        assertThat(config).contains("proxy_set_header X-Forwarded-Proto $scheme;");
    }

    @Test
    void nginxServesFrontendAndDedicatedHealthCheck() throws IOException {
        String config = Files.readString(NGINX_CONF);

        assertThat(config).contains("location = /nginx-health");
        assertThat(config).contains("return 200 \"ok\\n\";");
        assertThat(config).contains("location /");
        assertThat(config).contains("proxy_pass http://ssafy_frontend;");
    }

    @Test
    void nginxAppliesBaselineSecurityHeaders() throws IOException {
        String config = Files.readString(NGINX_CONF);

        assertThat(config).contains("add_header X-Content-Type-Options \"nosniff\" always;");
        assertThat(config).contains("add_header X-Frame-Options \"DENY\" always;");
        assertThat(config).contains("add_header Referrer-Policy \"strict-origin-when-cross-origin\" always;");
        assertThat(config).contains("add_header Permissions-Policy");
    }
}
