package com.example.Ecommerce_Muebleria.Notificaciones.controllers;

import com.example.Ecommerce_Muebleria.Notificaciones.services.NotificacionAppService;
import com.example.Ecommerce_Muebleria.Notificaciones.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/notifications")
@RequiredArgsConstructor // Lombok crea el constructor automáticamente para los private final
public class NotificationController {

    // Unificamos las dependencias con private final
    private final NotificationService notificationService;
    private final NotificacionAppService notificacionAppService;

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal OAuth2User principal) {

        String token = payload.get("token");

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        String userEmail = principal.getAttribute("email");

        if (token != null && !token.isEmpty() && userEmail != null) {
            notificationService.saveToken(token, userEmail);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/mis-notificaciones")
    public String verTodasLasNotificaciones(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return "redirect:/";
        }

        // 🚀 CORRECCIÓN: Extraemos el email real en lugar del ID de Auth0
        String userEmail = principal.getAttribute("email");
        if (userEmail == null) {
            userEmail = principal.getName(); // Fallback por si acaso
        }

        // Ahora busca en la BD las notificaciones que coincidan con "silveroosmar911@gmail.com"
        model.addAttribute("todasLasNotificaciones", notificacionAppService.obtenerTodas(userEmail));
        notificacionAppService.marcarTodasComoLeidas(userEmail);

        return "mis-notificaciones";
    }
}