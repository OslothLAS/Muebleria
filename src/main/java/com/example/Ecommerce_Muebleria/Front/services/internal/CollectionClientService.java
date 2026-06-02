package com.example.Ecommerce_Muebleria.Front.services.internal;

import com.example.Ecommerce_Muebleria.BackProducts.repositories.CollectionRepository;
import com.example.Ecommerce_Muebleria.BackProducts.repositories.ProductRepository;
import com.example.Ecommerce_Muebleria.Front.dtos.CollectionRequest;
import com.example.Ecommerce_Muebleria.Front.services.ImageService;
import com.example.Ecommerce_Muebleria.entities.commons.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import com.example.Ecommerce_Muebleria.entities.commons.Collection;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CollectionClientService {

    // 🚀 INYECCIONES LOCALES (Cero red, cero HTTP)
    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageService imageService;

    // --- 1. GUARDAR NUEVA COLECCIÓN ---
    public void saveCollection(String name, String description, List<Long> productIds, MultipartFile imageFile) {
        log.info("📁 Iniciando guardado de colección: '{}'", name);

        Collection col = new Collection();
        col.setName(name);
        col.setDescription(description);

        // Subida de imagen a Cloudinary
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = imageService.uploadImg(imageFile);
                col.setImageUrl(imageUrl); // ⚠️ Ajustá a col.setImagenUrl() si tu entidad usa otro nombre
            }
        } catch (Exception e) {
            log.error("❌ Error al subir la imagen: {}", e.getMessage());
            throw new RuntimeException("Error guardando la imagen de la colección", e);
        }

        // Vinculación de productos (Optimizado con JPA)
        if (productIds != null && !productIds.isEmpty()) {
            List<Product> products = productRepository.findAllById(productIds);
            col.setProducts(products); // ⚠️ Ajustá a col.setProductos() si es necesario
        }

        collectionRepository.save(col);
        log.info("✅ Colección '{}' guardada exitosamente en la BD.", name);
    }

    // --- 2. LECTURA ---
    public List<Collection> getAllCollections() {
        return collectionRepository.findAll();
    }

    public List<Collection> getActiveCollections() {
         return collectionRepository.findByActiveTrue();
    }

    public Collection getCollectionById(Long id) {
        return collectionRepository.findById(id).orElse(null);
    }

    // --- 3. BORRADO ---
    public void deleteCollection(Long id) {
        log.info("🗑️ Borrando colección ID: {}", id);
        collectionRepository.deleteById(id);

        // 💡 Si usás borrado lógico, cambialo por esto:
        // Collection col = collectionRepository.findById(id).orElseThrow();
        // col.setActive(false);
        // collectionRepository.save(col);
    }

    // --- 4. FORMULARIO DE EDICIÓN Y ACTUALIZACIÓN ---
    public CollectionRequest getCollectionRequestById(Long id) {
        Collection col = getCollectionById(id);
        if (col == null) return null;

        CollectionRequest req = new CollectionRequest();
        req.setName(col.getName());
        req.setDescription(col.getDescription());

        // Mapeamos los IDs de los productos para que el Frontend los marque como seleccionados
        if (col.getProducts() != null) {
            List<Long> ids = col.getProducts().stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());
            req.setProductIds(ids);
        }
        return req;
    }

    public void updateCollection(Long id, String name, String description, List<Long> productIds, MultipartFile file) {
        log.info("✏️ Actualizando colección ID: {}", id);

        Collection col = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Colección no encontrada"));

        col.setName(name);
        col.setDescription(description);

        // Si mandaron una imagen nueva, la subimos y pisamos la vieja
        try {
            if (file != null && !file.isEmpty()) {
                String imageUrl = imageService.uploadImg(file);
                col.setImageUrl(imageUrl);
            }
        } catch (Exception e) {
            log.error("❌ Error al actualizar la imagen: {}", e.getMessage());
        }

        // Actualizamos la lista de productos
        if (productIds != null) {
            List<Product> products = productRepository.findAllById(productIds);
            col.setProducts(products);
        } else {
            col.getProducts().clear(); // Si desmarcaron todos los productos
        }

        collectionRepository.save(col);
        log.info("✅ Colección actualizada exitosamente.");
    }
}