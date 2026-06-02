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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        return productRepository.findByIsNewTrueAndActivoTrue();
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
            case "low-price" -> Sort.by(Sort.Direction.ASC, "price");
            case "high-price" -> Sort.by(Sort.Direction.DESC, "price");
            case "name-asc" -> Sort.by(Sort.Direction.ASC, "name");
            case "name-desc" -> Sort.by(Sort.Direction.DESC, "name");
            // "new" cae en default: ordenamos por el ID más alto (los últimos creados)
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
}