package com.pritam.dochelp.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class projectConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(
                        new Info()
                                .title("Dochelp Backend")
                                .description("Dochelp- Ai document healper || Backend API")
                                .version("1.0.0")
                                .contact(
                                        new Contact().name("Pritam Purkait").email("pritampurkait5533@gmail.com")
                                )
        );
    }
}
