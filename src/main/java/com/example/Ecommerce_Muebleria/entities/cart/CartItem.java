package com.example.Ecommerce_Muebleria.entities.cart;

import com.example.Ecommerce_Muebleria.entities.commons.Product;
import com.fasterxml.jackson.annotation.JsonIgnore; // Importante para evitar bucles infinitos
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "cart_item")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private int quantity;

    // 🚀 AGREGÁ ESTO:
    // @Transient le dice a JPA: "No busques esta columna en MySQL, es solo para el código Java"
    @Transient
    private Product productDetail;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    private Cart cart;
}