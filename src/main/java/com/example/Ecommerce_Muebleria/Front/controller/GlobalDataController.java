package com.example.Ecommerce_Muebleria.Front.controller;

import com.example.Ecommerce_Muebleria.Front.services.CartService;
import com.example.Ecommerce_Muebleria.Front.services.internal.CollectionClientService;
import com.example.Ecommerce_Muebleria.Front.services.internal.WishlistService;
import com.example.Ecommerce_Muebleria.entities.cart.Cart;
import com.example.Ecommerce_Muebleria.entities.cart.CartItem;
import com.example.Ecommerce_Muebleria.entities.commons.Collection;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.*;

@ControllerAdvice
public class GlobalDataController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CollectionClientService collectionClientService;
    @Autowired
    private WishlistService wishlistService;

    // 1. Obtener el nombre directamente de Auth0 (Sin Cookies)
    @ModelAttribute("currentUsername")
    public String getCurrentUser(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser != null) {
            // Sacamos el nombre que configuraste en Auth0
            return oidcUser.getGivenName() != null ? oidcUser.getGivenName() : oidcUser.getNickName();
        }
        return null;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model,
                                    @AuthenticationPrincipal OidcUser oidcUser,
                                    HttpSession session) {

        Cart cart = null;
        Map<Long, Integer> productQuantities = new HashMap<>();
        List<Long> favoritesIds = new ArrayList<>();

        // 🚀 1. Obtenemos el ID (GUEST_ o Auth0) SIEMPRE
        String userId = getCartId(oidcUser, session);

        // 🚀 2. CARGAMOS EL CARRITO PARA TODOS (Invitado o Logueado)
        try {
            cart = cartService.getCart(userId);
            if (cart != null && cart.getItems() != null) {
                for (CartItem item : cart.getItems()) {
                    Long pid = (item.getProductDetail() != null)
                            ? item.getProductDetail().getId()
                            : item.getProductId();
                    if (pid != null) {
                        productQuantities.put(pid, item.getQuantity());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error cargando carrito global: " + e.getMessage());
        }

        // 🚀 3. FAVORITOS: Solo si está logueado (La Wishlist sí requiere Auth0)
        if (oidcUser != null) {
            try {
                favoritesIds = wishlistService.getFavoriteIds();
            } catch (Exception e) {
                System.err.println("❌ Error cargando favoritos: " + e.getMessage());
            }
        }

        // 4. Blindaje final
        if (cart == null) {
            cart = new Cart();
            cart.setItems(new ArrayList<>());
        }

        List<Collection> collections = collectionClientService.getActiveCollections();
        model.addAttribute("collections", collections);
        model.addAttribute("myCart", cart);
        model.addAttribute("cartCount", (cart.getItems() != null) ? cart.getItems().size() : 0);
        model.addAttribute("productQuantities", productQuantities);
        model.addAttribute("favoritesIds", favoritesIds);

    }

    private String getCartId(OidcUser oidcUser, HttpSession session) {
        if (oidcUser != null) {
            return oidcUser.getSubject(); // Usuario real de Auth0
        }
        // Si no hay login, usamos el ID de la sesión del navegador
        return "GUEST_" + session.getId();
    }

}