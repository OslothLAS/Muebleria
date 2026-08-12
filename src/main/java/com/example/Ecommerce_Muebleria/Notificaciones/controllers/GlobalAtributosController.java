package com.example.Ecommerce_Muebleria.Notificaciones.controllers;

import com.example.Ecommerce_Muebleria.Notificaciones.services.NotificacionAppService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice
public class GlobalAtributosController {

    private final NotificacionAppService notificacionService;

    public GlobalAtributosController(NotificacionAppService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @ModelAttribute
    public void agregarAtributosGlobales(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            String userId = principal.getName();

         model.addAttribute("cantNotificaciones", notificacionService.contarNoLeidas(userId));
            model.addAttribute("ultimasNotificaciones", notificacionService.obtenerUltimas5(userId));
        } else {
            // Si no está logueado, mandamos cero y lista vacía
            model.addAttribute("cantNotificaciones", 0);
            model.addAttribute("ultimasNotificaciones", null);
        }
    }
}