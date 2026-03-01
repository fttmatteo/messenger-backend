package app.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger/OpenAPI para documentación de la API.
 */
@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                final String securitySchemeName = "bearerAuth";

                return new OpenAPI()
                                .info(new Info()
                                                .title("Messenger Backend API")
                                                .version("1.10.5")
                                                .description("API REST para la gestión de mensajeros, entregas y seguimiento en tiempo real.")
                                                .contact(new Contact()
                                                                .name("PLAK")
                                                                .url("https://plak.digital")
                                                                .email("contacto@plak.digital"))
                                                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                                .components(new Components()
                                                .addSecuritySchemes(securitySchemeName,
                                                                new SecurityScheme()
                                                                                .name(securitySchemeName)
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("Ingrese su token JWT aquí. Ejemplo: eyJhbGciOi...")));
        }
}
