package com.example.Ecommerce_Muebleria.entities.cart;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private int quantity;
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;
}