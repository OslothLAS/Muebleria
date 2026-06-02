package com.example.Ecommerce_Muebleria.config;

import org.springframework.beans.factory.annotation.Value; // 👈 Clave para Docker
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;

@Configuration
@EnableWebSecurity
public class AppConfig {

    // 1. Mantené solo lo esencial para el cliente HTTP si es que consumís APIs externas (ej: Mercado Pago)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    // 2. ⚠️ ELIMINAR O COMENTAR ESTO:
    // Al ser un monolito, no necesitamos un WebClient para hablar con nosotros mismos.
    // Usar servicios (@Service) es mucho más eficiente.
    /*
    @Bean
    public WebClient cartWebClient() {
        return WebClient.builder().baseUrl(cartServiceUrl).build();
    }

    @Bean
    public WebClient wishlistWebClient(...) {
        // ...
    }
    */
}