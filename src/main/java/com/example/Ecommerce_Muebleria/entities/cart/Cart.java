package com.example.Ecommerce_Muebleria.entities.cart;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList; // <--- Importante
import java.util.List;

@Entity
@Data
@Table(name = "cart") // O el nombre que tengas
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    // ERROR ESTABA AQUÍ: private List<CartItem> items;
    // SOLUCIÓN: Inicialízala para que nunca sea null
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
}