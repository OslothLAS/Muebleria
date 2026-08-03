package com.example.Ecommerce_Muebleria.entities.mensajeria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEmailMessage {
    private Long orderId;
    private String userEmail;
    private String userName;
    private BigDecimal totalAmount;
}