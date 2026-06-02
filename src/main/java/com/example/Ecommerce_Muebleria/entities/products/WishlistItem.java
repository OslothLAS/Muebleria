package com.example.Ecommerce_Muebleria.entities.products;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wishlist_items")
@Data
@NoArgsConstructor
public class WishlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId; // El ID de la cookie
    private Long productId;

    public WishlistItem(String userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }
}