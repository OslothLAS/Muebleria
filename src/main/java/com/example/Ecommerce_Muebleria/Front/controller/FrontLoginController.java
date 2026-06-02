package com.example.Ecommerce_Muebleria.Front.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontLoginController {

    // 🚀 Inyectamos las credenciales para el logout global de Auth0
    @Value("${spring.security.oauth2.client.registration.auth0.client-id}")
    private String clientId;

    @Value("${auth0.domain.url:https://dev-q5auxkp2cqakq6jd.us.auth0.com}")
    private String auth0Domain;


    /**
     * Ya no necesitamos el @PostMapping /auth/login-submit.
     * Al entrar a /login, simplemente redirigimos a la raíz
     * para que Spring Security dispare el flujo de Auth0.
     */
    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/";
    }

    /**
     * LOGOUT UNIFICADO
     * Limpia la sesión de Spring y le avisa a Auth0 para cerrar todo.
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return "redirect:/";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}