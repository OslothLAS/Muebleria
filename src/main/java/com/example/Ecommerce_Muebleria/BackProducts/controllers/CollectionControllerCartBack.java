package com.example.Ecommerce_Muebleria.BackProducts.controllers;

import com.example.Ecommerce_Muebleria.entities.commons.Collection;
import com.example.Ecommerce_Muebleria.BackProducts.services.CollectionServiceProductBack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/collections")
public class CollectionControllerCartBack {

    @Autowired
    private CollectionServiceProductBack collectionServiceProductBack;

    // 📝 Definimos el logger para ver todo en la consola del 8080
    private static final Logger log = LoggerFactory.getLogger(CollectionControllerCartBack.class);

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCollection(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "productIds", required = false) List<Long> productIds,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {

        log.info("🚀 Petición recibida en el Backend (8080) para crear colección: '{}'", name);

        // 🛡️ Debug de parámetros recibidos
        log.info("📝 Descripción: {}", (description != null ? description : "VACÍA"));
        log.info("📦 IDs de productos recibidos: {}", (productIds != null ? productIds : "NINGUNO"));
        log.info("🖼️ Archivo de imagen: {}", (imageFile != null ? imageFile.getOriginalFilename() + " (" + imageFile.getSize() + " bytes)" : "SIN ARCHIVO"));

        List<Long> safeProductIds = (productIds != null) ? productIds : new ArrayList<>();
        String safeDescription = (description != null) ? description : "";

        try {
            log.info("⚙️ Llamando al servicio de guardado...");
            Collection saved = collectionServiceProductBack.save(name, safeDescription, safeProductIds, imageFile);

            log.info("✅ Colección guardada con éxito. ID generado: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            log.error("❌ ERROR CRÍTICO al procesar la colección '{}':", name);
            log.error("💥 Causa: {}", e.getMessage());
            e.printStackTrace(); // Esto imprime el rastro completo en la consola

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en el backend (8080): " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCollection(@PathVariable Long id) {
        log.info("🔍 Buscando colección ID: {}", id);
        return collectionServiceProductBack.findById(id)
                .map(coll -> {
                    log.info("✅ Colección '{}' encontrada con {} productos.", coll.getName(), coll.getProducts().size());
                    return ResponseEntity.ok(coll);
                })
                .orElseGet(() -> {
                    log.warn("⚠️ Colección ID {} no existe.", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    public ResponseEntity<List<Collection>> getAllCollections() {
        log.info("📂 [GET] Petición recibida para listar todas las colecciones.");

        try {
            List<Collection> collections = collectionServiceProductBack.findAll(); // Asegurate de tener este método en el Service

            log.info("✅ Se encontraron {} colecciones en la base de datos.", collections.size());

            // Si no hay nada, devolvemos una lista vacía (200 OK) pero avisamos en el log
            if (collections.isEmpty()) {
                log.warn("⚠️ La lista de colecciones está vacía.");
            }

            return ResponseEntity.ok(collections);

        } catch (Exception e) {
            log.error("❌ ERROR al listar colecciones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Collection> update(@PathVariable Long id, @RequestBody Collection collection) {
        return ResponseEntity.ok(collectionServiceProductBack.update(id, collection));
    }

    // --- BORRAR ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        collectionServiceProductBack.delete(id);
        return ResponseEntity.noContent().build();
    }

}