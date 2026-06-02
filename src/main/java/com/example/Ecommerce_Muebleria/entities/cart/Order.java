package com.example.Ecommerce_Muebleria.entities.cart;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private LocalDateTime dateCreated;
    private BigDecimal totalAmount;
    private String status;


    private String shippingAddress;
    private String zipCode;
    private String city;
    private String referencesInfo;

    // --- DATOS DE FACTURACIÓN (Billing) ---
    private String billingAddress;
    private String billingZipCode;
    private String billingCity;
    private String billingReferencesInfo;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
}