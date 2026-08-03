package com.example.Ecommerce_Muebleria.BackCartOrder.controllers;

import com.example.Ecommerce_Muebleria.BackCartOrder.services.OrderServiceCartBack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class CheckoutController {

    @Autowired
    private OrderServiceCartBack orderServiceCartBack;

    @GetMapping("/cart/pending")
    public String handlePendingPayment(
            @RequestParam(value = "collection_id", required = false) String paymentId,
            @RequestParam(value = "external_reference", required = false) String safeUserId) {

        if (paymentId == null || safeUserId == null) {
            // Manejo de error si Mercado Pago no manda los datos
            return "redirect:/cart?error=invalid_pending_data";
        }

        // 1. Reconstruimos el ID del usuario (volvemos a poner la barra de Auth0 si la tenía)
        String userId = safeUserId.replace("_", "|");

        // 2. Vamos a buscar a Mercado Pago los datos de envío que guardaste en la metadata
        Map<String, String> shipping = orderServiceCartBack.getShippingDataFromPayment(paymentId);

        // 3. Guardamos la orden en tu base de datos marcándola como PENDIENTE
        orderServiceCartBack.saveOrderFromCart(
                userId,
                shipping.get("address"),
                shipping.get("zip"),
                shipping.get("city"),
                "PENDING" // 🚀 El estado clave para el historial
        );

        // 4. Redirigimos al usuario a su historial de compras
        return "redirect:/my-orders"; // Ajustá esta URL a la ruta real de tu frontend
    }
}