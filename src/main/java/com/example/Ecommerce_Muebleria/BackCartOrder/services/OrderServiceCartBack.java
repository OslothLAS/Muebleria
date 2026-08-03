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

    @Autowired private CartServiceCartBack cartServiceCartBack;

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
        // Ejecutamos la consulta y pedimos solo la primera página con un límite de 4 resultados
        return orderItemRepository.findFrequentlyBoughtTogether(productId, PageRequest.of(0, 4));
    }


    @Value("${mp.access.token}") private String accessToken;
    @Value("${app.front.url}") private String frontUrl;

    public String createCheckoutPreference(String userId, String address, String zipCode, String city) {
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

        // Recomendación: Reemplazar el string fijo por la variable frontUrl cuando pases a producción
        String baseUrl = "https://isopachous-echo-unapplauded.ngrok-free.dev";

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(baseUrl + "/cart/success")
                .failure(baseUrl + "/cart/failure")
                .pending(baseUrl + "/cart/pending")
                .build();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("shipping_address", address);
        metadata.put("zip_code", zipCode);
        metadata.put("city", city);
        metadata.put("user_id", userId);

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
    }

    @Transactional
    // 🚀 Modificación: Agregamos el parámetro 'status'
    public void saveOrderFromCart(String userId, String address, String zipCode, String city, String status) {
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
        order.setShippingAddress(address);
        order.setZipCode(zipCode);
        order.setCity(city);
        order.setDateCreated(LocalDateTime.now());
        order.setStatus(status); // 🚀 El estado ahora es dinámico

        Order finalOrder = order;
        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> {
            OrderItem oi = new OrderItem();
            oi.setProductId(cartItem.getProductId());
            oi.setQuantity(cartItem.getQuantity());

            // 🚀 Modificación: Prevención de NullPointerException en el precio
            BigDecimal itemPrice = cartItem.getProductDetail() != null ?
                    cartItem.getProductDetail().getPrice() :
                    BigDecimal.ZERO;
            oi.setPrice(itemPrice);

            oi.setOrder(finalOrder);
            return oi;
        }).toList();

        order.setOrderItems(orderItems);
        order.setTotalAmount(calculateTotal(cart.getItems()));

        order = orderRepository.save(order);

        // 🚀 Modificación: Solo mandamos a la cola de correos si el pago está aprobado
        if ("APPROVED".equalsIgnoreCase(status)) {
            OrderEmailMessage emailMessage = new OrderEmailMessage(
                    order.getId(),
                    userId,
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

    public Map<String, String> getShippingDataFromPayment(String paymentId) {
        try {
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.parseLong(paymentId));
            Map<String, Object> metadata = payment.getMetadata();

            Map<String, String> shippingData = new HashMap<>();

            // 🚀 Modificación: Prevención de NullPointerException al leer la metadata
            if (metadata != null) {
                Object addressObj = metadata.get("shipping_address");
                Object zipObj = metadata.get("zip_code");
                Object cityObj = metadata.get("city");

                shippingData.put("address", addressObj != null ? addressObj.toString() : "Sin dirección");
                shippingData.put("zip", zipObj != null ? zipObj.toString() : "Sin CP");
                shippingData.put("city", cityObj != null ? cityObj.toString() : "Sin Ciudad");
            } else {
                shippingData.put("address", "Sin dirección");
                shippingData.put("zip", "Sin CP");
                shippingData.put("city", "Sin Ciudad");
            }

            return shippingData;
        } catch (Exception e) {
            System.err.println("❌ Error al recuperar metadatos de MP: " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}