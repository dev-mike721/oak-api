package com.abarrotes.oakapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // No registrar manejadores de recursos estáticos para rutas de API
        // Spring Boot por defecto registra /** para recursos estáticos
        // No registramos nada para /api/**
    }
}
