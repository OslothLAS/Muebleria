package com.example.Ecommerce_Muebleria.BackCartOrder.services;

import com.example.Ecommerce_Muebleria.entities.cart.Cart;
import com.example.Ecommerce_Muebleria.entities.cart.CartItem;
import com.example.Ecommerce_Muebleria.entities.cart.Order;
import com.example.Ecommerce_Muebleria.entities.cart.OrderItem;
import com.example.Ecommerce_Muebleria.BackCartOrder.repositories.CartRepository;
import com.example.Ecommerce_Muebleria.BackCartOrder.repositories.OrderRepository;
import com.example.Ecommerce_Muebleria.entities.commons.Product;
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
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceCartBack {

    @Autowired private CartServiceCartBack cartServiceCartBack;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WebClient productWebClient; // El que configuramos con Token Relay

    @Value("${mp.access.token}") private String accessToken;
    @Value("${app.front.url}") private String frontUrl;

    public String createCheckoutPreference(String userId, String address, String zipCode, String city) {
        MercadoPagoConfig.setAccessToken(accessToken);

        // 1. Buscamos el carrito (ya sea de invitado o logueado)
        Cart cart = cartServiceCartBack.getCartByUserId(userId);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío.");
        }

        // 2. Armamos los items para Mercado Pago
        List<PreferenceItemRequest> itemsForMp = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            try {
                // Llamada "limpia" al micro 8080 para obtener precio y nombre actualizados
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

        // 3. URLs de retorno (Ngrok o URL final)
        String baseUrl = "https://isopachous-echo-unapplauded.ngrok-free.dev";

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(baseUrl + "/cart/success")
                .failure(baseUrl + "/cart/failure")
                .pending(baseUrl + "/cart/pending")
                .build();

        // 4. METADATA: La clave para el invitado
        // Guardamos todo acá porque cuando Mercado Pago nos avise del éxito,
        // solo nos dará un ID de pago, y con ese ID recuperaremos estos datos.
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("shipping_address", address);
        metadata.put("zip_code", zipCode);
        metadata.put("city", city);
        metadata.put("user_id", userId); // 🚀 Guardamos el ID híbrido (GUEST_ o Auth0)

        // Limpiamos el ID para el external_reference (MP no quiere caracteres raros)
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

    public List<Order> findByUserId(String userId){
        return orderRepository.findByUserId(userId);
    };

    @Transactional
    public void saveOrderFromCart(String userId, String address, String zipCode, String city) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado para ID: " + userId));

        // Hidratamos para tener los precios reales al momento del cierre
        for (CartItem item : cart.getItems()) {
            Product dto = productWebClient.get()
                    .uri("http://localhost:8080/api/products/" + item.getProductId())
                    .retrieve()
                    .bodyToMono(Product.class)
                    .block();
            item.setProductDetail(dto);
        }

        Order order = new Order();
        order.setUserId(userId); // 🚀 Se guarda como "GUEST_..." o "auth0|..."
        order.setShippingAddress(address);
        order.setZipCode(zipCode);
        order.setCity(city);
        order.setDateCreated(LocalDateTime.now());
        order.setStatus("APPROVED");

        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            OrderItem oi = new OrderItem();
            oi.setProductId(cartItem.getProductId());
            oi.setQuantity(cartItem.getQuantity());
            oi.setPrice(cartItem.getProductDetail().getPrice());
            oi.setOrder(order);
            return oi;
        }).toList();

        order.setOrderItems(orderItems);
        order.setTotalAmount(calculateTotal(cart.getItems()));

        orderRepository.save(order);

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

    public Map<String, String> getShippingDataFromPayment(String paymentId) {
        try {
            // 1. Configuramos el cliente de pagos
            PaymentClient client = new PaymentClient();

            // 2. Buscamos el pago en los servidores de Mercado Pago
            Payment payment = client.get(Long.parseLong(paymentId));

            // 3. Extraemos los metadatos que enviamos en el paso anterior
            Map<String, Object> metadata = payment.getMetadata();

            Map<String, String> shippingData = new HashMap<>();
            shippingData.put("address", metadata.get("shipping_address").toString());
            shippingData.put("zip", metadata.get("zip_code").toString());
            shippingData.put("city", metadata.get("city").toString());

            return shippingData;
        } catch (Exception e) {
            System.err.println("❌ Error al recuperar metadatos de MP: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

}