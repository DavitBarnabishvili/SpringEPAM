package com.gym.crm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI gymCrmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gym CRM REST API")
                        .description("REST API for Gym CRM System - Managing Trainees, Trainers, and Training Sessions")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Gym CRM Team")
                                .email("support@gymcrmepam.com")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Provide the JWT token here.")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"));
    }
}