package com.example.Ecommerce_Muebleria.BackProducts.controllers;

import com.example.Ecommerce_Muebleria.entities.commons.Product;
import com.example.Ecommerce_Muebleria.entities.products.Question;
import com.example.Ecommerce_Muebleria.BackProducts.services.ProductServiceProductBack;
import com.example.Ecommerce_Muebleria.BackProducts.services.QuestionServiceProductBack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/backend/products")
public class ProductApiControllerCartBack {


    @Autowired
    private ProductServiceProductBack productServiceProductBack;

    @Autowired
    private QuestionServiceProductBack questionServiceProductBack;

    @GetMapping

    public List<Product> getAllProducts() {
        return productServiceProductBack.findAllProducts();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productServiceProductBack.findById(id)
                .map(product -> {
                    product.setName(productDetails.getName());
                    product.setDescription(productDetails.getDescription());
                    product.setPrice(productDetails.getPrice());
                    product.setStock(productDetails.getStock());
                    // Importante: Actualizar también los campos técnicos
                    product.setModel(productDetails.getModel());
                    product.setMaterialStructure(productDetails.getMaterialStructure());
                    product.setMaxWeight(productDetails.getMaxWeight());
                    // Si la imagen nueva viene nula, mantenemos la que ya tenía
                    if (productDetails.getImageUrl() != null) product.setImageUrl(productDetails.getImageUrl());

                    return ResponseEntity.ok(productServiceProductBack.save(product));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/new")

    public List<Product> findNewProducts() {
        return productServiceProductBack.findNewProducts();

    }

    @GetMapping("/{id}")

    public Product getProductById(@PathVariable Long id) {
        return productServiceProductBack.findProductById(id);
    }

    @GetMapping("/destacados")

    public List<Product> getDestacados() {

        return productServiceProductBack.findByEsDestacadoTrue();
    }


    private Sort getSortObj(String sort) {

        switch (sort) {
            case "price_asc":
                return Sort.by("price").ascending(); // Ordenar por precio menor a mayor
            case "price_desc":
                return Sort.by("price").descending(); // Ordenar por precio mayor a menor
            case "new":
            default:
                return Sort.by("id").descending(); // Por defecto: Los últimos cargados primero
        }

    }

    @GetMapping("/search")

    public Map<String, Object> searchProducts(

            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "new") String sort) { // Recibimos el parámetro

        Sort sortObj = getSortObj(sort);


        Page<Product> productPage = productServiceProductBack.searchProducts(keyword, PageRequest.of(page, size, sortObj));

        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent());
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());


        return response;

    }




    @GetMapping("/paginated")

    public Map<String, Object> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "new") String sort) { // Recibimos el parámetro


        Sort sortObj = getSortObj(sort);

        Page<Product> productPage = productServiceProductBack.findAll(PageRequest.of(page, size, sortObj));

        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent());
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());


        return response;

    }


    @PostMapping
    public ResponseEntity<Product> saveProduct(@RequestBody Product product) {

        try {
            Product saved = productServiceProductBack.save(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        Product product = productServiceProductBack.findById(id).orElseThrow();

        product.setActivo(false); // Lo marcamos como no disponible
        productServiceProductBack.save(product);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/active")
    public List<Product> getActiveProducts() {
        return productServiceProductBack.getProductosActivos();
    }

    @PutMapping("/restore/{id}")
    public ResponseEntity<Void> restoreProduct(@PathVariable Long id) {
        Product product = productServiceProductBack.findById(id)
                .orElseThrow();

        product.setActivo(true); // Lo activamos de nuevo
        productServiceProductBack.save(product);

        return ResponseEntity.ok().build();
    }


    // En ProductApiController.java del Backend (8080)
    // Asegurate de tener definido el Logger al principio de la clase:
// private static final Logger log = LoggerFactory.getLogger(ProductApiController.class);

    @PostMapping("/{id}/questions")
    public ResponseEntity<Void> addQuestion(@PathVariable Long id, @RequestBody Question questionDto) {
        log.info("📥 Recibiendo petición POST para nueva pregunta. Producto ID: {}", id);

        try {
            return productServiceProductBack.findById(id)
                    .map(product -> {
                        // 1. Mapeo de DTO a Entidad
                        Question question = new Question();
                        question.setText(questionDto.getText());
                        question.setUserId(questionDto.getUserId());
                        question.setUserName(questionDto.getUserName());
                        question.setProduct(product);

                        // 2. Persistencia
                        questionServiceProductBack.save(question);

                        // 🚀 LOG DE ÉXITO REAL
                        log.info("✅ ¡ÉXITO! Pregunta guardada en la base de datos.");
                        log.info("   - Producto: {} (ID: {})", product.getName(), id);
                        log.info("   - Usuario: {}", questionDto.getUserName());

                        return new ResponseEntity<Void>(HttpStatus.CREATED);
                    })
                    .orElseGet(() -> {
                        log.warn("⚠️ No se pudo guardar la pregunta: El producto ID {} NO EXISTE en la DB.", id);
                        return ResponseEntity.notFound().build();
                    });

        } catch (Exception e) {
            log.error("💥 ERROR CRÍTICO en el proceso de guardado: {}", e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    @PostMapping("/questions/{questionId}/answer")
    public ResponseEntity<Void> addAnswer(@PathVariable Long questionId, @RequestBody String answer) {
        log.info("📥 Recibiendo respuesta para la pregunta ID: {}", questionId);

        return questionServiceProductBack.findById(questionId)
                .map(question -> {
                    question.setAnswer(answer); // 👈 Seteamos el campo que antes estaba en null
                    questionServiceProductBack.save(question);
                    log.info("✅ Respuesta guardada con éxito.");
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        log.info("🗑️ Admin solicitó borrar la pregunta ID: {}", questionId);

        return questionServiceProductBack.findById(questionId)
                .map(question -> {
                    questionServiceProductBack.delete(question);
                    log.info("✅ Pregunta eliminada de la base de datos.");
                    return ResponseEntity.noContent().<Void>build(); // 204 No Content
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/{id}/update-notes")
    public ResponseEntity<Void> updateSellerNotes(@PathVariable Long id, @RequestBody String notes) {
        log.info("📝 Actualizando notas del vendedor para el producto ID: {}", id);

        return productServiceProductBack.findById(id)
                .map(product -> {
                    product.setSellerNotes(notes); // 💾 Guardamos el HTML
                    productServiceProductBack.save(product);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/{id}/update-specs")
    public ResponseEntity<Void> updateSpecs(@PathVariable Long id, @RequestBody Product productDetails) {
        log.info("📥 Recibiendo actualización técnica para el producto ID: {}", id);

        return productServiceProductBack.findById(id).map(product -> {
            // 🚀 Pisamos solo los campos técnicos con lo que viene del Front
            product.setModel(productDetails.getModel());
            product.setMaterialStructure(productDetails.getMaterialStructure());
            product.setMaterialUpholstery(productDetails.getMaterialUpholstery());
            product.setHeight(productDetails.getHeight());
            product.setWidth(productDetails.getWidth());
            product.setDepth(productDetails.getDepth());
            product.setMaxWeight(productDetails.getMaxWeight());
            product.setWarranty(productDetails.getWarranty());

            productServiceProductBack.save(product);
            log.info("✅ Ficha técnica actualizada en DB.");
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }


}