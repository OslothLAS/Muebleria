package com.example.Ecommerce_Muebleria.Front.services;


import com.example.Ecommerce_Muebleria.BackCartOrder.services.CartServiceCartBack;
import com.example.Ecommerce_Muebleria.entities.cart.Cart;
import com.example.Ecommerce_Muebleria.entities.cart.CartItem;
import com.example.Ecommerce_Muebleria.entities.commons.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService {

    // 🚀 1. INYECTAMOS LA LÓGICA DE BASE DE DATOS DEL CARRITO (Ex-Microservicio)
    @Autowired
    private CartServiceCartBack cartServiceCartBack;

    // 🚀 2. INYECTAMOS EL SERVICIO DE PRODUCTOS LOCAL (Para armar el DTO de la vista)
    @Autowired
    private ProductService productService;

    public Cart getCart(String cartId) {
        // ✅ Llamada en memoria a la Base de Datos
        Cart cart = cartServiceCartBack.getCartByUserId(cartId);

        if (cart != null && cart.getItems() != null) {
            cart.getItems().forEach(item -> {
                try {
                    // ✅ Llamada en memoria para traer la info del mueble (precio, nombre, img)
                    Product detail = productService.findProductById(item.getProductId());
                    item.setProductDetail(detail);
                } catch (Exception e) {
                    System.err.println("⚠️ No se pudo obtener detalle para producto " + item.getProductId());
                }
            });
        }
        return cart != null ? cart : new Cart();
    }

    public void addToCart(Long productId, Integer quantity, String cartId) {
        int qty = (quantity == null) ? 1 : quantity;
        // ✅ Ejecución directa, sin latencia
        cartServiceCartBack.addItemToCart(cartId, productId, qty);
    }

    public void updateQuantity(Long productId, int delta, String cartId) {
        // ✅ Ejecución directa
        cartServiceCartBack.updateItemQuantityDelta(cartId, productId, delta);
    }

    public void removeProduct(Long productId, String cartId) {
        try {
            // ✅ Ejecución directa
            cartServiceCartBack.removeItemFromCart(cartId, productId);
            System.out.println("✅ Producto " + productId + " borrado para el ID: " + cartId);
        } catch (Exception e) {
            System.err.println("❌ Error en el borrado local del carrito: " + e.getMessage());
        }
    }

    public void clearCart(String cartId) {
        try {
            cartServiceCartBack.emptyCart(cartId);
        } catch (Exception e) {
            System.err.println("❌ No se pudo vaciar el carrito: " + e.getMessage());
        }
    }

    // --- Lógica de negocio (Cálculos internos en memoria) ---

    public Integer getProductQuantity(Long productId, String cartId) {
        Cart cart = this.getCart(cartId);
        if (cart == null || cart.getItems() == null) return 0;

        return cart.getItems().stream()
                .filter(item -> item.getProductDetail() != null &&
                        item.getProductDetail().getId() != null &&
                        item.getProductDetail().getId().equals(productId))
                .map(CartItem::getQuantity)
                .findFirst()
                .orElse(0);
    }

    public BigDecimal calculateTotal(Cart cart) {
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return cart.getItems().stream()
                .map(item -> {
                    if (item.getProductDetail() == null) return BigDecimal.ZERO;
                    BigDecimal price = item.getProductDetail().getPrice();
                    return price.multiply(new BigDecimal(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}