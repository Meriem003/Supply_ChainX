package com.supplychainx.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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
                    "**Sécurité :**\n" +
                    "Les endpoints nécessitent les headers suivants :\n" +
                    "- `X-User-Email` : Email de l'utilisateur\n" +
                    "- `X-User-Password` : Mot de passe de l'utilisateur\n\n" +
                    "**Compte admin par défaut :**\n" +
                    "- Email: `admin@supplychainx.com`\n" +
                    "- Password: `admin123`"
                )
                .termsOfService("https://www.supplychainx.com/terms")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}
