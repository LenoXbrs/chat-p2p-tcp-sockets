package br.com.chat.peer.chatpeer.application.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CHAT P2P")
                        .description("Chat comunicação via tcp/ip p2p.\n\n"
                            )
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Equipe Luis Eduardo ")
                                .email("drumondexp@gmail.com")
                                .url(""))
                        .license(new License()
                                .name("All Rights Reserved")
                                .url("https://chatp2p.com.br/terms")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentação da API")
                        .url(""));


    }
}

