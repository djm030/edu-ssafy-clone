package com.edussafy.backend.docs;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DockerComposeRuntimeHardeningTest {

    private static final Path COMPOSE = Path.of("..", "compose.yml");

    @Test
    void appProfileServicesPreventPrivilegeEscalation() throws IOException {
        String compose = Files.readString(COMPOSE);

        assertServiceContains(compose, "backend", "no-new-privileges:true");
        assertServiceContains(compose, "frontend", "no-new-privileges:true");
        assertServiceContains(compose, "nginx", "no-new-privileges:true");
    }

    @Test
    void backendDropsLinuxCapabilitiesBecauseItDoesNotBindPrivilegedPorts() throws IOException {
        String compose = Files.readString(COMPOSE);

        assertServiceContains(compose, "backend", "cap_drop:");
        assertServiceContains(compose, "backend", "- ALL");
        assertServiceContains(compose, "backend", "curl -fsS http://localhost:8080/api/readiness >/dev/null");
    }

    @Test
    void mysqlHealthcheckDoesNotDependOnMutableRootPassword() throws IOException {
        String compose = Files.readString(COMPOSE);

        String mysql = serviceBlock(compose, "mysql");
        assertThat(mysql)
                .contains("mysqladmin ping --protocol=tcp -h 127.0.0.1 --silent")
                .doesNotContain("MYSQL_PWD")
                .doesNotContain("-uroot");
    }

    private static void assertServiceContains(String compose, String serviceName, String expected) {
        String serviceBlock = serviceBlock(compose, serviceName);
        assertThat(serviceBlock).contains(expected);
    }

    private static String serviceBlock(String compose, String serviceName) {
        String marker = "  " + serviceName + ":";
        StringBuilder block = new StringBuilder();
        boolean collecting = false;

        for (String line : compose.split("\\R")) {
            if (line.equals(marker)) {
                collecting = true;
            } else if (collecting && line.matches("  [A-Za-z0-9_-]+:")) {
                break;
            }
            if (collecting) {
                block.append(line).append('\n');
            }
        }

        assertThat(block).as("service %s exists", serviceName).isNotEmpty();
        return block.toString();
    }
}
