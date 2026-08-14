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
            System.out.println("🌱 Iniciando carga de datos con imágenes de catálogo de la mueblería...");

            // --- 1. PRODUCTOS VIEJOS (4) ---
            crearProducto("Sillón YPF dark wood", "Estructura de madera maciza tono oscuro", "/assets/images/collection/Sillon-YPF-dark-wood.jpg", new BigDecimal("185000"), 45, false, "Sofás y Sillones");
            crearProducto("Mesa baja dark wood", "Ideal para centros de living reducidos", "/assets/images/collection/Mesa-baja-dark-wood.jpg", new BigDecimal("75000"), 35, false, "Mesas");
            crearProducto("Estante bodega de vinos", "Exhibidor flotante en madera natural", "/assets/images/collection/Estante-de-vinos.jpg", new BigDecimal("35000"), 60, false, "Organización");
            crearProducto("Mesa ratona empotrable", "Diseño funcional que optimiza espacio", "/assets/images/collection/Mesa-ratona-empotrable.jpg", new BigDecimal("95000"), 40, false, "Mesas");

            // --- 2. PRODUCTOS DESTACADOS (4) ---
            crearProducto("Mesa ratona varillada", "Diseño moderno con base cilíndrica de varillas", "/assets/images/collection/Mesa-ratona-varillada.jpg", new BigDecimal("135000"), 50, true, "Mesas");
            crearProducto("Juego estantería y cómoda", "Combo completo para tu sala de estar", "/assets/images/collection/Juego-completo-de-estanteria-y-comoda.jpg", new BigDecimal("320000"), 50, true, "Módulos y Combos");
            crearProducto("Biblioteca montessori", "Mueble accesible para el cuarto de los chicos", "/assets/images/collection/Biblioteca-montesori.jpg", new BigDecimal("85000"), 50, true, "Línea Infantil");
            crearProducto("Banco con zapatero", "Mueble recibidor tono Dark Wood", "/assets/images/collection/Banco-con-zapatero-dark-wood.jpg", new BigDecimal("115000"), 50, true, "Recibidores");

            // --- 3. PRODUCTOS NUEVOS (6) --- (Días de antigüedad en 0)
            crearProducto("Mesa montessori regulable", "Crece junto con tus hijos", "/assets/images/collection/Mesa-montesori-regulable.jpg", new BigDecimal("65000"), 0, false, "Línea Infantil");
            crearProducto("Estante con llavero", "Recibidor de pared práctico y minimalista", "/assets/images/collection/Estante-con-llavero.jpg", new BigDecimal("22000"), 0, false, "Organización");
            crearProducto("Bandeja de centro de sofá", "Apoyabrazos rígido de madera para bebidas", "/assets/images/collection/Bandeja-de-centro-de-sofa.jpg", new BigDecimal("18000"), 0, false, "Accesorios");
            crearProducto("Mesa de luz para sofá", "Organizador lateral estilo Dark Wood", "/assets/images/collection/Mesa-de-luz-de-sofa-dark-wood.jpg", new BigDecimal("55000"), 0, false, "Mesas");
            crearProducto("Organizador dark wood", "Estantería baja multipropósito", "/assets/images/collection/Organizador-dark-wood.jpg", new BigDecimal("89000"), 0, false, "Organización");
            crearProducto("Mesa de luz larga", "Amplia superficie de apoyo y guardado", "/assets/images/collection/Mesa-de-luz-larga.jpg", new BigDecimal("72000"), 0, false, "Dormitorio");

            // --- 4. RELLENO / STANDARD (6) ---
            crearProducto("Caja de maquillaje", "Organizador de madera con divisiones", "/assets/images/collection/Caja-de-maquillaje.jpg", new BigDecimal("25000"), 70, false, "Accesorios");
            crearProducto("Silla plegable con colcha", "Asiento auxiliar cómodo y fácil de guardar", "/assets/images/collection/Silla-plegable-con-colcha.jpg", new BigDecimal("48000"), 85, false, "Sillas");
            crearProducto("Silla de exterior", "Madera tratada resistente a la intemperie", "/assets/images/collection/Siila-exterior.jpg", new BigDecimal("55000"), 45, false, "Exterior");
            crearProducto("Zapatero de pared", "Estructura flotante para ahorrar espacio", "/assets/images/collection/Zapatero-de-pared.jpg", new BigDecimal("42000"), 65, false, "Organización");
            crearProducto("Estante varillado", "Estante decorativo con respaldo de varillas", "/assets/images/collection/Estante-varillado.jpg", new BigDecimal("38000"), 33, false, "Decoración");
            crearProducto("Set de bancos de exterior", "Par de bancos rústicos para jardín o balcón", "/assets/images/collection/Bancos-exterior.jpg", new BigDecimal("145000"), 50, false, "Exterior");

            // Actualizamos estados para que los viejos dejen de ser "New"
            productScheduler.updateNewStatus();

            System.out.println("✅ Seeder finalizado: 20 Productos creados con imágenes del catálogo.");
        }
    }

    private void crearProducto(String nombre, String desc, String imagen, BigDecimal precio, int diasAntiguedad, boolean esDestacado, String categoria) {
        Product p = new Product(nombre, desc, imagen, precio);
        p.setCreatedAt(LocalDateTime.now().minusDays(diasAntiguedad));
        p.setNewProduct(true);
        p.setEsDestacado(esDestacado);
        p.setCategory(categoria);
        productRepository.save(p);
    }
}