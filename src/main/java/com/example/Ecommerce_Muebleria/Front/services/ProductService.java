package com.example.Ecommerce_Muebleria.Front.services;

import com.example.Ecommerce_Muebleria.BackProducts.repositories.ProductRepository;
import com.example.Ecommerce_Muebleria.BackProducts.repositories.QuestionRepository;
import com.example.Ecommerce_Muebleria.Front.dtos.QuestionDTO;
import com.example.Ecommerce_Muebleria.entities.commons.Product;
// IMPORTANTE: Asegurate de importar tus Repositorios correctos de JPA
import com.example.Ecommerce_Muebleria.entities.products.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {

    // 🚀 1. INYECCIÓN DIRECTA A LA BASE DE DATOS (CHAU RED, CHAU TIMEOUTS)
    @Autowired
    private ProductRepository productRepository;

     @Autowired private QuestionRepository questionRepository;


    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> findAllActiveProducts() {
        return productRepository.findByActivoTrue();
    }

    public List<Product> findIsNew() {

        return productRepository.findByNewProductTrueAndActivoTrue();
    }

    // 🚀 NUEVO: MÉTODO MAESTRO PARA FILTRAR EN LA TIENDA
    public Map<String, Object> filterProductsPaginated(String keyword, List<String> categorias,
                                                       BigDecimal minPrice, BigDecimal maxPrice,
                                                       Boolean inStock, int page, String sortParam) {
        try {
            // 1. Traemos todos los productos activos
            List<Product> allActive = findAllActiveProducts();

            // 2. Aplicamos todos los filtros de una usando Streams
            List<Product> filtrados = allActive.stream()
                    .filter(p -> keyword == null || keyword.isEmpty() || p.getName().toLowerCase().contains(keyword.toLowerCase()))
                    .filter(p -> minPrice == null || p.getPrice().compareTo(minPrice) >= 0)
                    .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
                    .filter(p -> inStock == null || !inStock || (p.getStock() != null && p.getStock() > 0))
                    .filter(p -> categorias == null || categorias.isEmpty() ||
                            (p.getCategories() != null && p.getCategories().stream().anyMatch(categorias::contains)))
                    .collect(java.util.stream.Collectors.toList());

            // 3. Aplicamos el ordenamiento
            if ("price_asc".equals(sortParam)) {
                filtrados.sort(Comparator.comparing(Product::getPrice));
            } else if ("price_desc".equals(sortParam)) {
                filtrados.sort(Comparator.comparing(Product::getPrice).reversed());
            } else { // "new" por defecto
                filtrados.sort((p1, p2) -> Long.compare(p2.getId(), p1.getId()));
            }

            // 4. Paginación manual (12 productos por página)
            int pageSize = 12;
            int totalItems = filtrados.size();
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
            int start = Math.min(page * pageSize, totalItems);
            int end = Math.min((page + 1) * pageSize, totalItems);
            List<Product> pageContent = filtrados.subList(start, end);

            // 5. Devolvemos el mismo Map que espera tu HomeController
            Map<String, Object> response = new HashMap<>();
            response.put("products", pageContent);
            response.put("currentPage", page);
            response.put("totalPages", totalPages);
            response.put("totalItems", totalItems);
            return response;

        } catch (Exception e) {
            log.error("Error filtrando productos: {}", e.getMessage());
            return fallbackPagination();
        }
    }
    // --- BÚSQUEDA DE RECOMENDACIONES (ALGORITMO) ---
    public List<Product> findProductsByIds(List<Long> ids) {
        List<Product> recommended = new ArrayList<>();

        for (Long id : ids) {
            try {
                // 🚀 Reutilizamos tu método existente que ya sabe cómo ir al 8080
                Product p = findProductById(id);

                // Solo agregamos productos que existan y estén activos
                if (p != null && (p.getActivo() == null || p.getActivo())) {
                    recommended.add(p);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error obteniendo producto recomendado " + id + ": " + e.getMessage());
            }
        }
        return recommended;
    }

    public Product findProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> findFeaturedProducts() {
        return productRepository.findByEsDestacadoTrueAndActivoTrue(); // Reemplazar por tu query específica
    }

    public List<Product> getProductsForShop() {
        // Asumiendo que tenés un flag 'active' o similar
        // return productRepository.findByActiveTrue();
        return productRepository.findAll();
    }

    // --- 2. MÉTODOS DE PAGINACIÓN ---

    public Map<String, Object> findAllPaginated(int page, String sortParam) {
        try {
            Pageable pageable = PageRequest.of(page, 12, buildSort(sortParam));
            Page<Product> pageData = productRepository.findByActivoTrue(pageable);
            return buildPaginationResponse(pageData);
        } catch (Exception e) {
            log.error("Error paginando productos: {}", e.getMessage());
            return fallbackPagination();
        }
    }

    public Map<String, Object> searchProductsPaginated(String keyword, int page, String sortParam) {
        try {
            Pageable pageable = PageRequest.of(page, 12, buildSort(sortParam));
            // Asumiendo que tenés findByNameContainingIgnoreCase en tu ProductRepository
            Page<Product> pageData = productRepository.findByNameContainingIgnoreCaseAndActivoTrue(keyword, pageable);
            return buildPaginationResponse(pageData);
        } catch (Exception e) {
            log.error("Error en la búsqueda paginada: {}", e.getMessage());
            return fallbackPagination();
        }
    }

    // 🚀 MÉTODO MÁGICO: Traduce el string del frontend a instrucciones SQL reales
    private Sort buildSort(String sortParam) {
        if (sortParam == null) return Sort.by(Sort.Direction.DESC, "id");

        return switch (sortParam) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");   // Corregido
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price"); // Corregido
            case "name-asc" -> Sort.by(Sort.Direction.ASC, "name");
            case "name-desc" -> Sort.by(Sort.Direction.DESC, "name");
            // "new" (y cualquier otra cosa) cae en default: ordenamos por el ID más alto
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
    }

    // Helper para no repetir código
    private Map<String, Object> buildPaginationResponse(Page<Product> pageData) {
        Map<String, Object> response = new HashMap<>();
        response.put("products", pageData.getContent());
        response.put("currentPage", pageData.getNumber());
        response.put("totalPages", pageData.getTotalPages());
        response.put("totalItems", pageData.getTotalElements());
        return response;
    }

    private Map<String, Object> fallbackPagination() {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("products", Collections.emptyList());
        fallback.put("currentPage", 0);
        fallback.put("totalPages", 0);
        fallback.put("totalItems", 0);
        return fallback;
    }

    // --- 3. MÉTODOS DE ESCRITURA (GUARDADO / ACTUALIZACIÓN) ---

    // Unificamos los tres métodos que hacían lo mismo por red en uno solo de JPA
    public void saveProduct(Product product) {
        productRepository.save(product);
        log.info("✅ Producto guardado en la BD.");
    }

    public void sendProductToBackend(Product product) {
        saveProduct(product);
    }

    public void updateProductInBackend(Product product) {
        saveProduct(product);
    }

    public void updateProductNotes(Long productId, String notes) {
        productRepository.findById(productId).ifPresent(product -> {
            // product.setNotes(notes); // Descomentar si tenés el campo 'notes'
            productRepository.save(product);
            log.info("✅ Notas del producto {} actualizadas.", productId);
        });
    }

    public void updateProductSpecs(Product product) {
        productRepository.save(product);
        log.info("✅ Especificaciones del producto actualizadas.");
    }

    // --- 4. PREGUNTAS Y RESPUESTAS ---

    public void saveQuestion(Question questionDto) {
        log.info("🚀 Guardando pregunta para el producto ID: {}", questionDto.getProduct().getId());
        Product product = productRepository.findById(questionDto.getProduct().getId()).orElseThrow();
        Question question = new Question();
        question.setText(questionDto.getText());
        question.setProduct(product);
        questionRepository.save(question);

    }

    public void saveAnswer(Long questionId, String answerText) {
        log.info("🚀 Respondiendo la pregunta ID: {}", questionId);
        Question question = questionRepository.findById(questionId).orElseThrow();
        question.setAnswer(answerText);
        questionRepository.save(question);

    }

    public void deleteQuestion(Long questionId) {
        log.info("🗑️ Borrando pregunta ID: {}", questionId);
        questionRepository.deleteById(questionId);
    }

    // --- 5. BORRADO LÓGICO Y RESTAURACIÓN ---

    public void deactivateProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        // Asumo que tenés un campo booleano 'active' o 'estado'.
        // Cambialo por el nombre real de tu atributo.
        product.setActivo(false);
        productRepository.save(product);
        log.info("🚫 Producto ID {} desactivado", id);
    }

    public void restoreProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        product.setActivo(true);
        productRepository.save(product);
        log.info("♻️ Producto ID {} restaurado", id);
    }

    // --- PLAN B: FALLBACK POR CATEGORÍA ---
    // (Ajustá "String category" al tipo de dato real de tu categoría si es un Objeto/Enum)

    // --- PLAN B: FALLBACK POR CATEGORÍA (Filtrado en memoria) ---
    public List<Product> findProductsByCategory(String category, int limit) {
        try {
            List<Product> all = findAllActiveProducts();

            return all.stream()
                    // 🚀 Ahora verificamos si la lista contiene la categoría
                    .filter(p -> p.getCategories() != null && p.getCategories().contains(category))
                    .limit(limit)
                    .toList();

        } catch (Exception e) {
            log.error("⚠️ Error en filtrado por categoría: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // 🚀 NUEVO: Método optimizado para buscar los recomendados en el detalle del producto
    public List<Product> findRecommendedProducts(List<String> categories, Long currentProductId, int limit) {
        // Busca coincidencias, limitadas a 4 (o el límite que pases)
        List<Product> recomendados = productRepository.findDistinctByCategoriesInAndActivoTrue(
                categories,
                PageRequest.of(0, limit + 1) // Pedimos uno extra por si viene el producto actual
        );

        // Filtramos para que no se recomiende a sí mismo
        return recomendados.stream()
                .filter(p -> !p.getId().equals(currentProductId))
                .limit(limit)
                .toList();
    }
}