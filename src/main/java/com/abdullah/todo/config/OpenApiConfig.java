package com.abdullah.todo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 Configuration for Swagger UI.
 * 
 * Provides API metadata displayed in the Swagger UI interface.
 * Access the documentation at:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI todoOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Todo API")
                                                .description("A professional RESTful API for managing Todo items. " +
                                                                "Features include data persistence with H2 database, " +
                                                                "input validation, DTOs, and global error handling.")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Abdullah")
                                                                .email("chief.abdullah14@gmail.com"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8080")
                                                                .description("Development Server")));
        }
}
