package com.example.Ecommerce_Muebleria.Notificaciones.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_tokens")
@Data
@NoArgsConstructor
public class NotificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    // Acá guardamos el identificador del usuario de Auth0 (ej. el email o el subject)
    @Column(name = "user_email", nullable = false)
    private String userEmail;

    public NotificationToken(String token, String userEmail) {
        this.token = token;
        this.userEmail = userEmail;
    }
}