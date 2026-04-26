package com.edussafy.backend.docs.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI edussafyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("eduSSAFY Clone API")
                        .version("local")
                        .description("Swagger/OpenAPI documentation generated from the running eduSSAFY clone backend.")
                        .contact(new Contact().name("eduSSAFY clone")))
                .servers(List.of(new Server().url("/").description("Current host")));
    }
}
