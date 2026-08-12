package com.example.Ecommerce_Muebleria.Notificaciones.tokens;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_fcm_tokens")
@Getter @Setter
public class UserFcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    private String userEmail;

    private LocalDateTime createdAt = LocalDateTime.now();
}