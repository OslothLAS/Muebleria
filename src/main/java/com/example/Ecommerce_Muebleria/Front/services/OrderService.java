package com.example.Ecommerce_Muebleria.Front.services;

import com.example.Ecommerce_Muebleria.BackCartOrder.services.OrderServiceCartBack;
import com.example.Ecommerce_Muebleria.Notificaciones.services.NotificationService;
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

    @Autowired
    private NotificationService notificationService;

    // --- 1. TRAER UNA ORDEN ESPECÍFICA ---
    public Order getOrder(Long orderId) {
        return orderServiceCartBack.findById(orderId).orElseThrow(RuntimeException::new);
    }

    public List<Order> getOrders(String userId) {
        try {
            return orderServiceCartBack.findByUserId(userId);
        } catch (Exception e) {
            System.err.println("No se encontraron ordenes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void confirmPurchase(String paymentId, String cartId) {
        try {
            System.out.println("💳 Procesando confirmación de pago: " + paymentId + " para el usuario: " + cartId);

            Map<String, String> checkoutData = orderServiceCartBack.getShippingDataFromPayment(paymentId);

            // 🚀 2. CAPTURAMOS la orden que ahora nos devuelve el backend
            Order ordenGuardada = orderServiceCartBack.saveOrderFromCart(cartId, "APPROVED", checkoutData);

            // 🚀 3. DISPARAMOS LA NOTIFICACIÓN AL ADMIN
            try {
                notificationService.notificarCompraAlAdmin(ordenGuardada.getId(), ordenGuardada.getTotalAmount().doubleValue());
            } catch (Exception e) {
                System.err.println("⚠️ Error enviando notificación: " + e.getMessage());
            }

            System.out.println("✅ Backend notificado del éxito para: " + cartId);
        } catch (Exception e) {
            System.err.println("❌ Error confirmando compra: " + e.getMessage());
            throw e;
        }
    }

    // --- 1. Sobrecarga Original (5 parámetros) ---
    // Mantiene la compatibilidad con compras rápidas o integraciones anteriores.
    public String getPaymentLink(String address, String zipCode, String city, String cartId, String userEmail) {
        // Reutilizamos la nueva firma rellenando los campos faltantes con vacíos ("")
        // y clonando los datos de envío para la facturación.
        return this.getPaymentLink(
                address, zipCode, city, "", "",
                address, zipCode, city, "", "",
                cartId, userEmail
        );
    }


    // 2. Nueva Sobrecarga Completa (12 parámetros)
    // Atrapa todos los datos del nuevo formulario HTML que separó envío y facturación.
    public String getPaymentLink(
            String shippingAddress, String shippingZipCode, String shippingCity, String shippingBetweenStreets, String shippingReferencesInfo,
            String billingAddress, String billingZipCode, String billingCity, String billingBetweenStreets, String billingReferencesInfo,
            String cartId, String userEmail) {

        // Se comunica con la nueva firma del microservicio en el backend
        return orderServiceCartBack.createCheckoutPreference(
                shippingAddress, shippingZipCode, shippingCity, shippingBetweenStreets, shippingReferencesInfo,
                billingAddress, billingZipCode, billingCity, billingBetweenStreets, billingReferencesInfo,
                cartId, userEmail
        );
    }

    // --- 2. MOTOR DE RECOMENDACIONES ---
    public List<Long> getFrequentlyBoughtTogetherIds(Long productId) {
        try {
            return orderServiceCartBack.getFrequentlyBoughtTogetherIds(productId);
        } catch (Exception e) {
            System.err.println("⚠️ Error obteniendo recomendaciones: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // --- PARA TRANSFERENCIAS ---
    public void processTransferCheckout(
            String shippingAddress, String shippingZipCode, String shippingCity, String shippingBetweenStreets, String shippingReferencesInfo,
            String billingAddress, String billingZipCode, String billingCity, String billingBetweenStreets, String billingReferencesInfo,
            String cartId, String userEmail) {

        // 🚀 CAPTURAMOS la orden de transferencia
        Order ordenGuardada = orderServiceCartBack.createTransferOrder(
                shippingAddress, shippingZipCode, shippingCity, shippingBetweenStreets, shippingReferencesInfo,
                billingAddress, billingZipCode, billingCity, billingBetweenStreets, billingReferencesInfo,
                cartId, userEmail
        );

        // 🚀 DISPARAMOS LA NOTIFICACIÓN AL ADMIN
        try {
            notificationService.notificarCompraAlAdmin(ordenGuardada.getId(), ordenGuardada.getTotalAmount().doubleValue());
        } catch (Exception e) {
            System.err.println("⚠️ Error enviando notificación: " + e.getMessage());
        }
    }
}