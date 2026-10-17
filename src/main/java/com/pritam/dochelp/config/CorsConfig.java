package com.pritam.dochelp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@RequiredArgsConstructor
public class CorsConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        String[] allowOrigins = appProperties.getCors()
                .getAllowedOrigins()
                .split(",");
        String[] allowMethods = appProperties.getCors()
                .getAllowedMethods()
                .split(",");
        String[] allowHeaders = appProperties.getCors()
                .getAllowedHeaders()
                .split(",");

        registry.addMapping("/api/**")
                .allowedOrigins(allowOrigins)
                .allowedMethods(allowMethods)
                .allowedHeaders(allowHeaders)
                .allowCredentials(true);

    }
}
