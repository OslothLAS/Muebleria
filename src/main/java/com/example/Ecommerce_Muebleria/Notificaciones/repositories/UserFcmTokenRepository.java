package com.example.Ecommerce_Muebleria.Notificaciones.repositories;


import com.example.Ecommerce_Muebleria.Notificaciones.tokens.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {
    Optional<UserFcmToken> findByToken(String token);
}