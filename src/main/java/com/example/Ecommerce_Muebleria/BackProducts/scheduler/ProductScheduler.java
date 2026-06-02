package com.example.Ecommerce_Muebleria.BackProducts.scheduler;

import com.example.Ecommerce_Muebleria.BackProducts.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class ProductScheduler {

    @Autowired
    private ProductRepository productRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Medianoche
    @Transactional
    public void updateNewStatus() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30); // 30 días de antigüedad

        int updatedCount = productRepository.removeNewStatusFromOldProducts(cutoffDate);

        System.out.println("CRON: Se removió la etiqueta 'Nuevo' a " + updatedCount + " productos.");
    }

    @Component
    public class DatabaseCleanupTask {

        @Autowired
        private ProductRepository productRepository;

        // Se ejecuta todos los días a las 3:00 AM
        @Scheduled(cron = "0 0 3 * * *")
        @Transactional
        public void deleteInactiveProducts() {
            System.out.println("🧹 Iniciando limpieza de productos eliminados...");
            // Borra físicamente todos los que tengan activo = false
            productRepository.deleteByActivoFalse();
            System.out.println("✅ Limpieza completada.");
        }
    }
}