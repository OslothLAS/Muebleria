package com.example.Ecommerce_Muebleria.Front.services.internal;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

/**
 * Servicio de utilidad para simplificar llamadas HTTP.
 * Ahora delegamos la seguridad al WebClient configurado en AppConfig.
 */
@Service
public class WebApiCallerService {

    // 🚀 Usamos el WebClient que ya tiene el filtro de Auth0 (o el que prefieras)
    private final WebClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    // Inyectamos el WebClient que configuramos en AppConfig
    public WebApiCallerService(WebClient productWebClient) {
        this.webClient = productWebClient;
    }

    /**
     * GET Genérico
     */
    public <T> T get(String url, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * GET para Listas
     */
    public <T> List<T> getList(String url, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(responseType)
                .collectList()
                .block();
    }

    /**
     * POST Genérico
     */
    public <T> T post(String url, Object body, Class<T> responseType) {
        return webClient.post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * PUT Genérico
     */
    public <T> T put(String url, Object body, Class<T> responseType) {
        return webClient.put()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * DELETE Genérico
     */
    public void delete(String url) {
        webClient.delete()
                .uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    // --- Métodos de compatibilidad con RestTemplate (si todavía los usás) ---

    public <T> T getCart(String url, Class<T> responseType) {
        try {
            return restTemplate.getForObject(url, responseType);
        } catch (Exception e) {
            System.err.println("Error en GET: " + e.getMessage());
            return null;
        }
    }

    public void postVoid(String url, Object body) {
        try {
            restTemplate.postForEntity(url, body, Void.class);
        } catch (Exception e) {
            System.err.println("Error en POST VOID: " + e.getMessage());
        }
    }

    public <T> T getWithAuth(String url, String accessToken, Class<T> responseType) {
        try {
            return webClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (Exception e) {
            System.err.println("❌ Error en getWithAuth: " + e.getMessage());
            return null;
        }
    }
}