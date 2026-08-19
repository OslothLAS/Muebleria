package com.example.Ecommerce_Muebleria.Notificaciones.services;

import com.example.Ecommerce_Muebleria.BackProfiles.repositories.UserProfileRepository;
import com.example.Ecommerce_Muebleria.Notificaciones.entities.NotificacionApp;
import com.example.Ecommerce_Muebleria.Notificaciones.entities.NotificationToken;
import com.example.Ecommerce_Muebleria.Notificaciones.repositories.NotificacionAppRepository;
import com.example.Ecommerce_Muebleria.Notificaciones.repositories.NotificationTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final NotificationTokenRepository tokenRepository;

    @Autowired
    private  NotificacionAppRepository repository;

    @Autowired
    private NotificacionAppService notificacionAppService;

    @Autowired
    private NotificacionAppRepository appRepository;

    public NotificationService(NotificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }


    @Value("${app.admin.email}")
    private String adminEmail;

    public void notificarCompraAlAdmin(Long orderId, Double total) {

        // ¡Usamos el constructor que ya armaste en tu entidad!
        NotificacionApp noti = new NotificacionApp(
                adminEmail,                                                      // A quién va dirigida
                "🛒 Nueva compra registrada",                                    // Título
                "Se ha realizado el pedido #" + orderId + " por $" + total,      // Mensaje
                "/api/notifications/mis-notificaciones"                                      // URL de acción
        );

        notificacionAppService.marcarTodasComoLeidas(adminEmail);

        repository.save(noti);

    }

    @Transactional
    public void crearNotificacion(String email, String titulo, String mensaje, String url) {
        NotificacionApp nuevaNotificacion = new NotificacionApp(email, titulo, mensaje, url);
        repository.save(nuevaNotificacion);
        enviarNotificacion(email, titulo, mensaje);
    }



    @Transactional
    public void saveToken(String token, String userEmail) {
        // Primero verificamos que el token no exista ya en la base
        if (!tokenRepository.existsByToken(token)) {
            NotificationToken newToken = new NotificationToken(token, userEmail);
            tokenRepository.save(newToken);
            System.out.println("Token guardado exitosamente para el usuario: " + userEmail);
        } else {
            System.out.println("El token ya estaba registrado en el sistema.");
        }
    }

    public void enviarNotificacion(String userEmail, String titulo, String cuerpo) {
        // Buscamos todos los tokens de este usuario
        var tokens = tokenRepository.findByUserEmail(userEmail);

        if (tokens.isEmpty()) {
            System.out.println("El usuario no tiene tokens registrados.");
            return;
        }

        for (NotificationToken tokenEntity : tokens) {
            // Armamos la notificación
            Notification notification = Notification.builder()
                    .setTitle(titulo)
                    .setBody(cuerpo)
                    .build();

            // Ensamblamos el mensaje apuntando al token específico
            Message message = Message.builder()
                    .setNotification(notification)
                    .setToken(tokenEntity.getToken())
                    .build();

            try {
                // Lo disparamos a través de Firebase
                String response = FirebaseMessaging.getInstance().send(message);
                System.out.println("✅ Notificación enviada con éxito. ID: " + response);
            } catch (Exception e) {
                System.err.println("❌ Error al enviar notificación al token: " + tokenEntity.getToken());
                e.printStackTrace();
            }
        }
    }
}