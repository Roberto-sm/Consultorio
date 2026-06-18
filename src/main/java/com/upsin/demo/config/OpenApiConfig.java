package com.upsin.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String nombreSeguridad = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("API REST - Gestión de Consultorio Psicológico")
                        .version("1.0")
                        .description("Arquitectura Backend para la administración de citas, roles de usuario, expedientes clínicos y motor de búsqueda de especialistas.")
                        .contact(new Contact()
                                .name("Jesús Roberto Sandoval Martínez")
                                .email("robertosm14@outlook.com")
                                .url("https://github.com/Roberto-sm/Consultorio")))
                .addSecurityItem(new SecurityRequirement().addList(nombreSeguridad))
                .components(new Components()
                        .addSecuritySchemes(nombreSeguridad, new SecurityScheme()
                                .name(nombreSeguridad)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}