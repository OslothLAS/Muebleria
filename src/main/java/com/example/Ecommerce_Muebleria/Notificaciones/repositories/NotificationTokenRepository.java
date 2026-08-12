package com.example.Ecommerce_Muebleria.Notificaciones.repositories;

import com.example.Ecommerce_Muebleria.Notificaciones.entities.NotificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationTokenRepository extends JpaRepository<NotificationToken, Long> {

    // Nos sirve para no guardar el mismo token dos veces
    boolean existsByToken(String token);

    // Opcional: para buscar todos los tokens de un usuario cuando queramos mandarle un mensaje
    List<NotificationToken> findByUserEmail(String userEmail);
}