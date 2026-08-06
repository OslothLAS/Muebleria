package com.example.Ecommerce_Muebleria.BackCartOrder.services;

import com.example.Ecommerce_Muebleria.BackCartOrder.repositories.OrderItemRepository;
import com.example.Ecommerce_Muebleria.entities.cart.Cart;
import com.example.Ecommerce_Muebleria.entities.cart.CartItem;
import com.example.Ecommerce_Muebleria.entities.cart.Order;
import com.example.Ecommerce_Muebleria.entities.cart.OrderItem;
import com.example.Ecommerce_Muebleria.BackCartOrder.repositories.CartRepository;
import com.example.Ecommerce_Muebleria.BackCartOrder.repositories.OrderRepository;
import com.example.Ecommerce_Muebleria.entities.commons.Product;
import com.example.Ecommerce_Muebleria.entities.mensajeria.OrderEmailMessage;
import com.example.Ecommerce_Muebleria.entities.mensajeria.OrderMessagePublisher;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceCartBack {

    @Autowired
    private CartServiceCartBack cartServiceCartBack;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private WebClient productWebClient;

    @Autowired
    private OrderMessagePublisher orderMessagePublisher;

    public List<Long> getFrequentlyBoughtTogetherIds(Long productId) {
        return orderItemRepository.findFrequentlyBoughtTogether(productId, PageRequest.of(0, 4));
    }

    @Value("${mp.access.token}")
    private String accessToken;

    @Value("${app.front.url}")
    private String frontUrl;

    // 🚀 1. Recibe todos los parámetros divididos y los guarda en la metadata
    public String createCheckoutPreference(
            String shippingAddress, String shippingZipCode, String shippingCity, String shippingBetweenStreets, String shippingReferencesInfo,
            String billingAddress, String billingZipCode, String billingCity, String billingBetweenStreets, String billingReferencesInfo,
            String userId, String userEmail) {

        MercadoPagoConfig.setAccessToken(accessToken);

        Cart cart = cartServiceCartBack.getCartByUserId(userId);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío.");
        }

        List<PreferenceItemRequest> itemsForMp = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            try {
                Product prod = productWebClient.get()
                        .uri("http://localhost:8080/api/products/{id}", item.getProductId())
                        .retrieve()
                        .bodyToMono(Product.class)
                        .block();

                if (prod != null) {
                    itemsForMp.add(PreferenceItemRequest.builder()
                            .title(prod.getName())
                            .quantity(item.getQuantity())
                            .unitPrice(prod.getPrice())
                            .currencyId("ARS")
                            .build());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error obteniendo producto " + item.getProductId() + ": " + e.getMessage());
            }
        }

        String baseUrl = "https://isopachous-echo-unapplauded.ngrok-free.dev";

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(baseUrl + "/cart/success")
                .failure(baseUrl + "/cart/failure")
                .pending(baseUrl + "/cart/pending")
                .build();

        // 🚀 Agrupamos la info de entrega y facturación en el JSON de Mercado Pago
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("shipping_address", shippingAddress);
        metadata.put("shipping_zip", shippingZipCode);
        metadata.put("shipping_city", shippingCity);
        metadata.put("shipping_refs", "Entre: " + shippingBetweenStreets + " - Ref: " + shippingReferencesInfo);

        metadata.put("billing_address", billingAddress);
        metadata.put("billing_zip", billingZipCode);
        metadata.put("billing_city", billingCity);
        metadata.put("billing_refs", "Entre: " + billingBetweenStreets + " - Ref: " + billingReferencesInfo);

        metadata.put("user_id", userId);
        metadata.put("user_email", userEmail);

        String safeUserId = userId.replace("|", "_");

        PreferenceRequest request = PreferenceRequest.builder()
                .items(itemsForMp)
                .backUrls(backUrls)
                .metadata(metadata)
                .autoReturn("approved")
                .externalReference(safeUserId)
                .build();

        try {
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(request);
            return preference.getSandboxInitPoint();
        } catch (Exception ex) {
            throw new RuntimeException("Error en Mercado Pago: " + ex.getMessage());
        }
    }

    public List<Order> findByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional
    // 🚀 2. Ahora recibe el Map limpio directamente
    public void saveOrderFromCart(String userId, String status, Map<String, String> checkoutData) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado para ID: " + userId));

        for (CartItem item : cart.getItems()) {
            Product dto = productWebClient.get()
                    .uri("http://localhost:8080/api/products/" + item.getProductId())
                    .retrieve()
                    .bodyToMono(Product.class)
                    .block();
            item.setProductDetail(dto);
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(status);
        order.setDateCreated(LocalDateTime.now());

        // 🚀 Asignamos datos de Envío
        order.setShippingAddress(checkoutData.getOrDefault("shipping_address", "N/A"));
        order.setZipCode(checkoutData.getOrDefault("shipping_zip", "0000"));
        order.setCity(checkoutData.getOrDefault("shipping_city", "N/A"));
        order.setReferencesInfo(checkoutData.getOrDefault("shipping_refs", ""));

        // 🚀 Asignamos datos de Facturación
        order.setBillingAddress(checkoutData.getOrDefault("billing_address", "N/A"));
        order.setBillingZipCode(checkoutData.getOrDefault("billing_zip", "0000"));
        order.setBillingCity(checkoutData.getOrDefault("billing_city", "N/A"));
        order.setBillingReferencesInfo(checkoutData.getOrDefault("billing_refs", ""));

        Order finalOrder = order;
        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            OrderItem oi = new OrderItem();
            oi.setProductId(cartItem.getProductId());
            oi.setQuantity(cartItem.getQuantity());

            BigDecimal itemPrice = cartItem.getProductDetail() != null ?
                    cartItem.getProductDetail().getPrice() :
                    BigDecimal.ZERO;
            oi.setPrice(itemPrice);

            // 🚀 ESTA ES LA LÍNEA CLAVE PARA QUE EL MAIL TENGA EL NOMBRE
            String nombre = cartItem.getProductDetail() != null ? cartItem.getProductDetail().getName() : "Mueble sin nombre";
            System.out.println("🕵️ DIAGNÓSTICO - ID: " + cartItem.getProductId() + " | Nombre detectado: " + nombre);

            oi.setProductName(nombre);

            oi.setOrder(finalOrder);
            return oi;
        }).toList();

        order.setOrderItems(orderItems);
        order.setTotalAmount(calculateTotal(cart.getItems()));

        order = orderRepository.save(order);

        if ("APPROVED".equalsIgnoreCase(status)) {
            OrderEmailMessage emailMessage = new OrderEmailMessage(
                    order.getId(),
                    userId,
                    checkoutData.getOrDefault("email", "cliente@ejemplo.com"), // Usamos el correo del Map
                    "Cliente",
                    order.getTotalAmount()
            );
            orderMessagePublisher.publishOrderEmailEvent(emailMessage);
        }

        cartServiceCartBack.emptyCart(userId);
    }

    private BigDecimal calculateTotal(List<CartItem> items) {
        return items.stream()
                .map(item -> {
                    if (item.getProductDetail() == null) return BigDecimal.ZERO;
                    return item.getProductDetail().getPrice().multiply(new BigDecimal(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    // 🚀 3. Rescata toda la metadata de Mercado Pago y la pasa a un Map
    public Map<String, String> getShippingDataFromPayment(String paymentId) {
        try {
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.parseLong(paymentId));
            Map<String, Object> metadata = payment.getMetadata();

            Map<String, String> data = new HashMap<>();

            if (metadata != null) {
                data.put("shipping_address", metadata.getOrDefault("shipping_address", "Sin dirección").toString());
                data.put("shipping_zip", metadata.getOrDefault("shipping_zip", "Sin CP").toString());
                data.put("shipping_city", metadata.getOrDefault("shipping_city", "Sin Ciudad").toString());
                data.put("shipping_refs", metadata.getOrDefault("shipping_refs", "").toString());

                data.put("billing_address", metadata.getOrDefault("billing_address", "Sin dirección").toString());
                data.put("billing_zip", metadata.getOrDefault("billing_zip", "Sin CP").toString());
                data.put("billing_city", metadata.getOrDefault("billing_city", "Sin Ciudad").toString());
                data.put("billing_refs", metadata.getOrDefault("billing_refs", "").toString());

                data.put("email", metadata.getOrDefault("user_email", "cliente_sin_mail@ejemplo.com").toString());
            } else {
                data.put("shipping_address", "Sin dirección");
                data.put("shipping_zip", "Sin CP");
                data.put("shipping_city", "Sin Ciudad");
                data.put("shipping_refs", "");

                data.put("billing_address", "Sin dirección");
                data.put("billing_zip", "Sin CP");
                data.put("billing_city", "Sin Ciudad");
                data.put("billing_refs", "");

                data.put("email", "cliente_sin_mail@ejemplo.com");
            }

            return data;
        } catch (Exception e) {
            System.err.println("❌ Error al recuperar metadatos de MP: " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}