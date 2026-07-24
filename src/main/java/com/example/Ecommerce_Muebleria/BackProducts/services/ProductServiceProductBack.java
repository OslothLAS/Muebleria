package com.example.Ecommerce_Muebleria.BackProducts.services;


import com.example.Ecommerce_Muebleria.entities.commons.Product;
import com.example.Ecommerce_Muebleria.BackProducts.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceProductBack {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> findAll() {return productRepository.findAll();}

    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> findNewProducts() {
        return productRepository.findByNewProductTrue();
    }

    public Product findProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> findByEsDestacadoTrue() {return productRepository.findByEsDestacadoTrue();}

// En ProductService.java del Backend:

    public Page<Product> searchProducts(String keyword, Pageable pageable) {

        return productRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    @Transactional
    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(long id) {
        productRepository.deleteById(id);
    }
    public boolean existsById(Long id) {
       return productRepository.existsById(id);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductosActivos(){
        return productRepository.findByActivoTrue();
    }

}