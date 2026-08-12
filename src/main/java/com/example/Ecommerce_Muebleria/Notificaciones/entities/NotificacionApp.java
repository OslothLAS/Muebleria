package com.example.Ecommerce_Muebleria.Notificaciones.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones_app")
@Data
@NoArgsConstructor
public class NotificacionApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail; // A quién va dirigida

    @Column(nullable = false)
    private String titulo;

    @Column(length = 500)
    private String mensaje;

    private boolean leida = false;

    private LocalDateTime fecha = LocalDateTime.now();

    private String urlAccion; // Opcional: por si querés que al hacer clic los lleve a ver un producto o su pedido

    public NotificacionApp(String userEmail, String titulo, String mensaje, String urlAccion) {
        this.userEmail = userEmail;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.urlAccion = urlAccion;
    }
}