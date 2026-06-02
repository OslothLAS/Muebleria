package com.example.Ecommerce_Muebleria.Front.services;

import com.example.Ecommerce_Muebleria.BackCartOrder.services.OrderServiceCartBack;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.Ecommerce_Muebleria.entities.cart.Order;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class OrderService {

    @Autowired
    private OrderServiceCartBack orderServiceCartBack;


    // --- 1. TRAER UNA ORDEN ESPECÍFICA ---
    public Order getOrder(Long orderId) {
        return orderServiceCartBack.findById(orderId).orElseThrow(RuntimeException::new);
    }

    public List<Order> getOrders(String userId) {
        try {
            return orderServiceCartBack.findByUserId(userId);
        } catch (Exception e) {
            // Capturamos cualquier otro error (como que el micro 8082 esté caído)
            System.err.println("No se encontraron ordenes" + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void confirmPurchase(String paymentId, String cartId) { // 🚀 Ahora acepta 2 argumentos
        try {

            System.out.println("💳 Procesando confirmación de pago: " + paymentId + " para el usuario: " + cartId);

            Map<String, String> shipping = orderServiceCartBack.getShippingDataFromPayment(paymentId);

            orderServiceCartBack.saveOrderFromCart(
                    cartId,
                    shipping.getOrDefault("address", "Sin dirección"),
                    shipping.getOrDefault("zip", "0000"),
                    shipping.getOrDefault("city", "N/A")
            );

            System.out.println("✅ Backend notificado del éxito para: " + cartId);
        } catch (Exception e) {
            System.err.println("❌ Error confirmando compra: " + e.getMessage());
            throw e;
        }
    }

    public String getPaymentLink(String address, String zipCode, String city, String cartId) {
        return orderServiceCartBack.createCheckoutPreference(cartId, address, zipCode, city);
    }
}