package com.example.Ecommerce_Muebleria.entities.commons;


import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private Boolean active = true;

    @ManyToMany
    @JoinTable(
            name = "product_collection", // Tabla intermedia
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();

    public List<Long> getProductsId() {
        if (this.products == null || this.products.isEmpty()) {
            return List.of(); // Evitamos NullPointerException
        }

        return this.products.stream()
                .map(Product::getId) // Extraemos el ID de cada objeto Product
                .toList();           // Lo convertimos en una lista (Java 16+)
    }
}