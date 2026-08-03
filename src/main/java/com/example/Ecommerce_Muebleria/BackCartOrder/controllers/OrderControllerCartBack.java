package com.example.Ecommerce_Muebleria.BackCartOrder.controllers;



import com.example.Ecommerce_Muebleria.entities.cart.Order;
import com.example.Ecommerce_Muebleria.BackCartOrder.services.CartServiceCartBack;
import com.example.Ecommerce_Muebleria.BackCartOrder.services.OrderServiceCartBack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/order")
public class OrderControllerCartBack {

    @Autowired
    private OrderServiceCartBack orderServiceCartBack;

    @Autowired
    private CartServiceCartBack cartServiceCartBack;


    // En el controlador del Micro 8082
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable String userId) {
        List<Order> orders = orderServiceCartBack.findByUserId(userId);

        // TRUCO SENIOR: Siempre devolvé 200 OK con lista vacía, nunca 404 para búsquedas.
        return ResponseEntity.ok(orders != null ? orders : new ArrayList<>());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        // 🚀 IMPORTANTE: Debe retornar Order, NO List<Order>
        return orderServiceCartBack.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


// En el Micro 8082 - OrderController.java

    @PostMapping("/confirm") // 🚀 Cambiamos el nombre a uno más descriptivo
    public ResponseEntity<Void> confirmOrder(@RequestParam String paymentId,
                                             @RequestParam String cartId) { // 🚀 Recibimos cartId (GUEST_ o Auth0)

        System.out.println("💳 Procesando confirmación de pago: " + paymentId + " para el usuario: " + cartId);

        // 1. Pedimos a Mercado Pago los datos de envío (metadata)
        Map<String, String> shipping = orderServiceCartBack.getShippingDataFromPayment(paymentId);

        // 2. Guardamos la orden en la DB usando el ID híbrido
        // 2. Guardamos la orden en la DB usando el ID híbrido
        orderServiceCartBack.saveOrderFromCart(
                cartId,
                shipping.getOrDefault("address", "Sin dirección"),
                shipping.getOrDefault("zip", "0000"),
                shipping.getOrDefault("city", "N/A"),
                "APPROVED" // 🚀 Acá agregamos el quinto parámetro faltante
        );


        return ResponseEntity.ok().build();
    }

}
