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

            // 🚀 EXTRAEMOS EL EMAIL EN LUGAR DEL ID DE AUTH0
            String userEmail = principal.getAttribute("email");

            // Fallback por si algún usuario logueado no tiene el email público
            if (userEmail == null) {
                userEmail = principal.getName();
            }

            // Ahora el servicio busca correctamente por el correo
            model.addAttribute("cantNotificaciones", notificacionService.contarNoLeidas(userEmail));
            model.addAttribute("ultimasNotificaciones", notificacionService.obtenerUltimas5(userEmail));

        } else {
            // Si no está logueado, mandamos cero y lista vacía
            model.addAttribute("cantNotificaciones", 0);
            model.addAttribute("ultimasNotificaciones", null);
        }
    }
}