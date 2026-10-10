package com.example.Ecommerce_Muebleria.BackCartOrder.controllers;

import com.example.Ecommerce_Muebleria.BackCartOrder.services.OrderServiceCartBack;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

@Controller
public class CheckoutController {

    @Autowired
    private OrderServiceCartBack orderServiceCartBack;

    @GetMapping("/cart/pending")
    public String handlePendingPayment(
            @RequestParam(value = "collection_id", required = false) String paymentId,
            @AuthenticationPrincipal OidcUser oidcUser,
            HttpSession session) {

        if (paymentId == null) {
            // Manejo de error si Mercado Pago no manda los datos
            return "redirect:/cart?error=invalid_pending_data";
        }

        // 1. 🛡️ El carrito sale de la sesión de quien vuelve de Mercado Pago, NO del external_reference de la URL
        //    (antes cualquiera podía armar la URL y generar órdenes con el carrito de otro usuario)
        String userId = (oidcUser != null) ? oidcUser.getSubject() : "GUEST_" + session.getId();

        // Recarga de la página: la orden ya existe
        if (orderServiceCartBack.findByPaymentId(paymentId).isPresent()) {
            return "redirect:/my-orders";
        }

        try {
            // 2. Verificamos con Mercado Pago que el pago exista, esté pendiente y sea de este carrito
            Map<String, String> checkoutData = orderServiceCartBack.verifyPayment(
                    paymentId, userId, Set.of("pending", "in_process", "authorized"));

            // 🚀 3. Guardamos la orden marcándola como PENDIENTE
            orderServiceCartBack.saveOrderFromCart(userId, "PENDING", checkoutData, paymentId);
        } catch (Exception e) {
            System.err.println("❌ Pago pendiente rechazado: " + e.getMessage());
            return "redirect:/cart?error=invalid_pending_data";
        }

        // 4. Redirigimos al usuario a su historial de compras
        return "redirect:/my-orders";
    }
}
