package com.example.Ecommerce_Muebleria.Front.services.internal;


import com.example.Ecommerce_Muebleria.Front.dtos.AuthResponseDTO;
import com.example.Ecommerce_Muebleria.Front.dtos.UserRolesPermissionsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class LoginApiService {

    private final WebApiCallerService webApiCallerService;
    private final WebClient webClient;

    @Autowired
    public LoginApiService(WebApiCallerService webApiCallerService) {
        // 🚀 Ya no inyectamos @Value("${auth.service.url}")
        this.webClient = WebClient.builder().build();
        this.webApiCallerService = webApiCallerService;
    }

    /**
     * ⚠️ IMPORTANTE: Con Auth0, el login no ocurre acá.
     * Este método queda por compatibilidad pero no debería ser invocado.
     */
    public AuthResponseDTO login(String username, String password) {
        System.out.println("🚨 Intento de login manual interceptado. Redirigir a Auth0.");
        return null;
    }

    /**
     * Si necesitás obtener datos extra del usuario, usá el WebClient
     * inyectado con seguridad en lugar de este método manual.
     */
    public UserRolesPermissionsDTO getRolesPermisos(String accessToken) {
        return new UserRolesPermissionsDTO();
    }
}