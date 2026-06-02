package com.example.Ecommerce_Muebleria.BackCartOrder.controllers;

import com.example.Ecommerce_Muebleria.entities.cart.Cart;
import com.example.Ecommerce_Muebleria.BackCartOrder.services.CartServiceCartBack;
import com.example.Ecommerce_Muebleria.BackCartOrder.services.OrderServiceCartBack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartControllerCartBack {

    @Autowired
    private OrderServiceCartBack orderServiceCartBack;

    @Autowired
    private CartServiceCartBack cartServiceCartBack;

    @GetMapping
    public ResponseEntity<Cart> getCart(@RequestParam String cartId) {
        // 🚀 Ya no usamos el JWT, usamos el ID que nos manda el Front
        return ResponseEntity.ok(cartServiceCartBack.getCartByUserId(cartId));
    }

    // 2. Agregar un producto
    @PostMapping("/add")
    public ResponseEntity<Cart> addItem(
            @RequestParam Long productId,
            @RequestParam int quantity,
            @RequestParam String cartId) { // 🚀 ID híbrido (Invitado o Auth0)

        Cart updatedCart = cartServiceCartBack.addItemToCart(cartId, productId, quantity);
        return ResponseEntity.ok(updatedCart);
    }

    // 3. Quitar un producto
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> removeItem(
            @PathVariable Long productId,
            @RequestParam String cartId) {

        return ResponseEntity.ok(cartServiceCartBack.removeItemFromCart(cartId, productId));
    }

    // 4. Actualizar cantidad con DELTA
    @PostMapping("/update")
    public ResponseEntity<Cart> updateQuantity(
            @RequestParam Long productId,
            @RequestParam int delta,
            @RequestParam String cartId) {

        Cart updatedCart = cartServiceCartBack.updateItemQuantityDelta(cartId, productId, delta);
        return ResponseEntity.ok(updatedCart);
    }

    // 5. Checkout (Crear preferencia de pago)
    @PostMapping("/checkout")
    public ResponseEntity<String> getPaymentLink(
            @RequestParam String cartId, // 🚀 Ahora el checkout también usa el cartId
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String zipCode,
            @RequestParam(required = false) String city) {

        if (address == null || zipCode == null || city == null) {
            return ResponseEntity.badRequest().body("Error: Faltan datos de envío");
        }

        // Creamos la preferencia de Mercado Pago para este ID (sea invitado o no)
        String url = orderServiceCartBack.createCheckoutPreference(cartId, address, zipCode, city);
        return ResponseEntity.ok(url);
    }

    // 6. Vaciar el carrito
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@RequestParam String cartId) {
        System.out.println("🗑️ Vaciando carrito para el ID: " + cartId);
        cartServiceCartBack.emptyCart(cartId);
        return ResponseEntity.noContent().build();
    }
}