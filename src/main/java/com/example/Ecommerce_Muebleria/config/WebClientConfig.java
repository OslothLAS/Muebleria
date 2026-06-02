package com.example.Ecommerce_Muebleria.config;

import org.springframework.beans.factory.annotation.Value; // 👈 Importante
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // 🚀 Inyectamos la URL del micro de productos.
    // Fallback a localhost para cuando programas en IntelliJ sin Docker.
    @Value("${product.service.url:http://localhost:8080}")
    private String productServiceUrl;

    @Bean
    public WebClient productWebClient() {
        return WebClient.builder()
                .baseUrl(productServiceUrl) // 👈 Ahora es dinámico
                .filter((request, next) -> {
                    // 1. Buscamos la autenticación actual en el hilo de ejecución
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                    // 2. Si el usuario ESTÁ logueado (Auth0), le pasamos el token al 8080
                    if (auth instanceof JwtAuthenticationToken jwtAuth) {
                        String tokenValue = jwtAuth.getToken().getTokenValue();

                        ClientRequest filtered = ClientRequest.from(request)
                                .header("Authorization", "Bearer " + tokenValue)
                                .build();
                        return next.exchange(filtered);
                    }

                    // 3. Si es un INVITADO (no hay JWT), mandamos la petición limpia.
                    return next.exchange(request);
                })
                .build();
    }
}