package com.example.Ecommerce_Muebleria.BackProducts.services;

import com.example.Ecommerce_Muebleria.entities.commons.Collection;
import com.example.Ecommerce_Muebleria.entities.commons.Product;
import com.example.Ecommerce_Muebleria.BackProducts.repositories.CollectionRepository;
import com.example.Ecommerce_Muebleria.BackProducts.repositories.ProductRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CollectionServiceProductBack {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private ProductRepository productRepository;

    private final Cloudinary cloudinary;

    public CollectionServiceProductBack(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Optional<Collection> findById(Long id) {
        return collectionRepository.findById(id);
    }

    public List<Collection> findAll() {return collectionRepository.findAll();}



    @Transactional
    public Collection save(String name, String description, List<Long> productIds, MultipartFile file) throws IOException {
        Collection collection = new Collection();
        collection.setName(name);
        collection.setDescription(description);

        // 1. Subida a Cloudinary (Solo si hay archivo)
        if (file != null && !file.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder", "eden/collections"));
            collection.setImageUrl(uploadResult.get("secure_url").toString());
        } else {
            // Podés poner una imagen por defecto si querés
            collection.setImageUrl("https://res.cloudinary.com/demo/image/upload/sample.jpg");
        }

        // 2. Buscar productos
        if (!productIds.isEmpty()) {
            List<Product> selectedProducts = productRepository.findAllById(productIds);
            collection.setProducts(selectedProducts);
        }

        return collectionRepository.save(collection);
    }

    @Transactional
    public Collection update(Long id, Collection request) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Colección no encontrada"));

        // Actualizamos datos básicos
        collection.setName(request.getName());
        collection.setDescription(request.getDescription());

        // Solo actualizamos la imagen si viene una nueva URL de Cloudinary
        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            collection.setImageUrl(request.getImageUrl());
        }

        // Actualizamos la relación de productos
        if (request.getProductsId()!= null) {
            // Buscamos los productos nuevos y reemplazamos la lista
            List<Product> products = productRepository.findAllById(request.getProductsId());
            collection.setProducts(products);
        }

        return collectionRepository.save(collection);
    }

    // --- LÓGICA DE BORRADO ---
    @Transactional
    public void delete(Long id) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Colección no encontrada"));

        // Antes de borrar, desvinculamos los productos para no romper la integridad
        collection.getProducts().clear();

        collectionRepository.delete(collection);
    }

}