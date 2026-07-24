package com.example.Ecommerce_Muebleria.config;


import com.example.Ecommerce_Muebleria.entities.commons.Product;
import com.example.Ecommerce_Muebleria.BackProducts.repositories.ProductRepository;
import com.example.Ecommerce_Muebleria.BackProducts.scheduler.ProductScheduler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final ProductScheduler productScheduler;

    public DataSeeder(ProductRepository productRepository, ProductScheduler productScheduler) {
        this.productRepository = productRepository;
        this.productScheduler = productScheduler;
    }

    @Override
    public void run(String... args) throws Exception {
        if (productRepository.count() == 0) {
            System.out.println("🌱 Iniciando carga de datos con imágenes reales...");

            // NOTA: Usamos "/assets/images/collection/" asumiendo que "static.assets"
            // en tu IDE significa la carpeta "static/assets".

            // --- 1. PRODUCTOS VIEJOS (4) ---
            // Usamos arrivals1 a arrivals4
            crearProducto("Silla Vintage", "Estilo clásico restaurado", "/assets/images/collection/arrivals1.png", new BigDecimal("45.00"), 35, false);
            crearProducto("Mesa Roble", "Madera maciza antigua", "/assets/images/collection/arrivals2.png", new BigDecimal("120.00"), 40, false);
            crearProducto("Sillón Retro", "Diseño de los 70", "/assets/images/collection/arrivals3.png", new BigDecimal("80.00"), 50, false);
            crearProducto("Lámpara Antigua", "Iluminación cálida", "/assets/images/collection/arrivals4.png", new BigDecimal("30.00"), 32, false);

            // --- 2. PRODUCTOS DESTACADOS (4) ---
            // Usamos SillaBalli y arrivals5 a arrivals7
            crearProducto("Silla Bali", "Nuestra silla estrella", "/assets/images/collection/SillaBalli.png", new BigDecimal("219000"), 50, true);
            crearProducto("Mesa Ratona", "Ideal para living", "/assets/images/collection/arrivals5.png", new BigDecimal("95.00"), 50, true);
            crearProducto("Lámpara Arco", "Diseño moderno de pie", "/assets/images/collection/arrivals6.png", new BigDecimal("150.00"), 50, true);
            crearProducto("Silla Comedor", "Ergonómica y suave", "/assets/images/collection/arrivals7.png", new BigDecimal("65.00"), 50, true);

            // --- 3. PRODUCTOS NUEVOS (6) ---
            // Usamos arrivals8 y repetimos desde el 1
            crearProducto("Sofá Minimalista", "Gris claro 3 cuerpos", "/assets/images/collection/arrivals8.png", new BigDecimal("350.00"), 50, false);
            crearProducto("Butaca Madera", "Artesanal", "/assets/images/collection/arrivals1.png", new BigDecimal("110.00"), 50, false);
            crearProducto("Mesa de Luz", "Pequeña y práctica", "/assets/images/collection/arrivals2.png", new BigDecimal("55.00"), 0, false);
            crearProducto("Espejo Marco", "Decorativo circular", "/assets/images/collection/arrivals3.png", new BigDecimal("45.00"), 0, false);
            crearProducto("Estantería", "Hierro negro", "/assets/images/collection/arrivals4.png", new BigDecimal("85.00"), 0, false);
            crearProducto("Alfombra Yute", "Tejido natural", "/assets/images/collection/arrivals5.png", new BigDecimal("70.00"), 0, false);

            // --- 4. RELLENO / STANDARD (6) ---
            // Completamos repitiendo imágenes restantes
            crearProducto("Perchero Pie", "Madera natural", "/assets/images/collection/arrivals6.png", new BigDecimal("25.00"), 50, false);
            crearProducto("Set Cojines", "Pack x2 lino", "/assets/images/collection/arrivals7.png", new BigDecimal("35.00"), 39, false);
            crearProducto("Macetero Alto", "Cerámica blanca", "/assets/images/collection/arrivals8.png", new BigDecimal("20.00"), 120, false);
            crearProducto("Silla Oficina", "Con ruedas", "/assets/images/collection/SillaBalli.png", new BigDecimal("120.00"), 90, false); // Repetimos SillaBalli
            crearProducto("Mesa Auxiliar", "Metal y vidrio", "/assets/images/collection/arrivals1.png", new BigDecimal("60.00"), 6, false);
            crearProducto("Cesta Mimbre", "Organizadora", "/assets/images/collection/arrivals2.png", new BigDecimal("15.00"), 90, false);

            // Actualizamos estados para que los viejos dejen de ser "New"
            productScheduler.updateNewStatus();

            System.out.println("✅ Seeder finalizado: 20 Productos creados con imágenes existentes.");
        }
    }

    private void crearProducto(String nombre, String desc, String imagen, BigDecimal precio, int diasAntiguedad, boolean esDestacado) {
        Product p = new Product(nombre, desc, imagen, precio);
        p.setCreatedAt(LocalDateTime.now().minusDays(diasAntiguedad));
        p.setNewProduct(true); // Se marca true inicialmente, el scheduler corrige si es viejo
        p.setEsDestacado(esDestacado);
        productRepository.save(p);
    }
}