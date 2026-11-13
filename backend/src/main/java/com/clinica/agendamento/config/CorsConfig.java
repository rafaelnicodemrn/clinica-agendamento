package com.clinica.agendamento.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS: o angular roda na porta 4200 e a api na 8080, como sao origens diferentes
// o navegador bloqueia as chamadas se a api nao liberar.
// Daria pra colocar @CrossOrigin em cada controller, mas como sao 3 controllers
// preferimos deixar uma configuracao global aqui (como foi falado em aula)
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
            }
        };
    }
}
