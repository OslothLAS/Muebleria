package com.example.Ecommerce_Muebleria.Notificaciones.services;

import com.example.Ecommerce_Muebleria.Notificaciones.entities.NotificacionApp;
import com.example.Ecommerce_Muebleria.Notificaciones.repositories.NotificacionAppRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificacionAppService {

    private final NotificacionAppRepository repository;

    public NotificacionAppService(NotificacionAppRepository repository) {
        this.repository = repository;
    }

    // Método para cuando quieras avisarle algo a un cliente (ej: desde tu panel de admin)
    @Transactional
    public void crearNotificacion(String email, String titulo, String mensaje, String url) {
        NotificacionApp noti = new NotificacionApp(email, titulo, mensaje, url);
        repository.save(noti);
    }
    public long contarNoLeidas(String email) {
        return repository.countByUserEmailAndLeidaFalse(email);
    }

    // Traemos solo las últimas 5 para que no explote el menú desplegable
    public List<NotificacionApp> obtenerUltimas5(String email) {
        List<NotificacionApp> todas = repository.findByUserEmailOrderByFechaDesc(email);
        return todas.size() > 5 ? todas.subList(0, 5) : todas;
    }

    public List<NotificacionApp> obtenerTodas(String email) {
        return repository.findByUserEmailOrderByFechaDesc(email);
    }

    // Marca todas las notificaciones pendientes como leídas
    @Transactional
    public void marcarTodasComoLeidas(String email) {
        List<NotificacionApp> noLeidas = repository.findByUserEmailOrderByFechaDesc(email)
                .stream()
                .filter(n -> !n.isLeida())
                .toList();

        for (NotificacionApp n : noLeidas) {
            n.setLeida(true);
        }

        repository.saveAll(noLeidas);
    }
}