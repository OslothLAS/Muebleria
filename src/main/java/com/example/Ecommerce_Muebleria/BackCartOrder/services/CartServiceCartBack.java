package com.example.Ecommerce_Muebleria.BackCartOrder.services;

import com.example.Ecommerce_Muebleria.entities.cart.Cart;
import com.example.Ecommerce_Muebleria.entities.cart.CartItem;
import com.example.Ecommerce_Muebleria.BackCartOrder.repositories.CartRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class CartServiceCartBack {

    @Autowired
    private CartRepository cartRepository;

    // 1. OBTENER O CREAR: Fundamental para que el invitado tenga "lugar" donde guardar items
    public Cart getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
    }

    // 2. AGREGAR ITEM: Usa el método anterior para asegurar que el carrito exista
    public Cart addItemToCart(String userId, Long productId, int quantity) {
        Cart cart = getCartByUserId(userId);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductId(productId);
            newItem.setCart(cart);
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }
        return cartRepository.save(cart);
    }

    // 3. ACTUALIZAR CANTIDAD (Versión limpia del método que tenías)
    public void updateQuantity(String userId, Long productId, int quantity) {
        Cart cart = getCartByUserId(userId);

        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    cartRepository.save(cart);
                });
    }

    // 4. ACTUALIZAR POR DELTA (+1 / -1)
    public Cart updateItemQuantityDelta(String userId, Long productId, int delta) {
        Cart cart = getCartByUserId(userId);

        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .ifPresentOrElse(
                        item -> {
                            int newQty = item.getQuantity() + delta;
                            if (newQty <= 0) cart.getItems().remove(item);
                            else item.setQuantity(newQty);
                        },
                        () -> {
                            if (delta > 0) addItemToCart(userId, productId, delta);
                        }
                );

        return cartRepository.save(cart);
    }

    public Cart removeItemFromCart(String userId, Long productId) {
        Cart cart = getCartByUserId(userId);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        return cartRepository.save(cart);
    }

    @Transactional
    public void emptyCart(String userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }
}