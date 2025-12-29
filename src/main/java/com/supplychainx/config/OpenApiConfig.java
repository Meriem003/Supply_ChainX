package com.supplychainx.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI supplyChainXOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Serveur de développement");

        Contact contact = new Contact();
        contact.setEmail("contact@supplychainx.com");
        contact.setName("Équipe SupplyChainX");
        contact.setUrl("https://www.supplychainx.com");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("SupplyChainX - API de Gestion de la Supply Chain")
                .version("1.0.0")
                .contact(contact)
                .description(
                    "API REST complète pour la gestion de la chaîne d'approvisionnement.\n\n" +
                    "**Modules disponibles :**\n" +
                    "- 🔵 **Approvisionnement** : Gestion des fournisseurs, matières premières et commandes d'approvisionnement\n" +
                    "- 🟢 **Production** : Gestion des produits finis, ordres de production et planification\n" +
                    "- 🟡 **Livraison** : Gestion des clients, commandes clients et livraisons\n" +
                    "- 🟣 **Utilisateurs** : Gestion des utilisateurs et des rôles\n\n" +
                    "**Sécurité JWT :**\n" +
                    "1. Login via `/auth/login` pour obtenir les tokens\n" +
                    "2. Utiliser le `accessToken` dans le header: `Authorization: Bearer <token>`\n" +
                    "3. Rafraîchir avec `/auth/refresh` quand le token expire\n\n" +
                    "**Comptes de test (password: `password123`) :**\n" +
                    "- ADMIN: `admin@supplychainx.com`\n" +
                    "- GESTIONNAIRE: `gestionnaire@supplychainx.com`\n" +
                    "- RESPONSABLE: `responsable@supplychainx.com`"
                )
                .termsOfService("https://www.supplychainx.com/terms")
                .license(mitLicense);

        // Define JWT security scheme for Swagger UI
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT access token obtained from /auth/login");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme));
    }
}
