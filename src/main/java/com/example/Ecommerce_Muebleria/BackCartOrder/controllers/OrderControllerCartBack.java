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

    // 🛡️ Se eliminó POST /api/order/confirm: aprobaba órdenes con cualquier paymentId/cartId sin verificar el pago.
    //    La confirmación se hace en /cart/success (CartController -> OrderService.confirmPurchase).
}