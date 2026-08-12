package com.example.Ecommerce_Muebleria.Notificaciones.repositories;

import com.example.Ecommerce_Muebleria.Notificaciones.entities.NotificacionApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionAppRepository extends JpaRepository<NotificacionApp, Long> {

    // Trae las notificaciones ordenadas por la más reciente
    List<NotificacionApp> findByUserEmailOrderByFechaDesc(String userEmail);

    // Cuenta cuántas campanitas rojas tiene pendientes
    long countByUserEmailAndLeidaFalse(String userEmail);
}