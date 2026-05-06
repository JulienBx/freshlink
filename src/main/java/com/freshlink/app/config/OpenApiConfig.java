package com.freshlink.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  private static final String BEARER_SCHEME = "bearerAuth";

  @Bean
  public OpenAPI freshlinkOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("FreshLink API")
                .description(
                    "API de gestion de recettes HelloFresh — import, lecture et (à venir)"
                        + " connexion supermarchés.")
                .version("v1")
                .contact(
                    new Contact().name("Julien Burlereaux").email("julien.burlereaux@gmail.com"))
                .license(new License().name("Propriétaire — usage personnel")))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
        .components(
            new Components()
                .addSecuritySchemes(
                    BEARER_SCHEME,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description(
                            "JWT applicatif obtenu via POST /api/auth/google."
                                + " Valeur sans le préfixe « Bearer ».")));
  }
}
