package com.example.Ecommerce_Muebleria.Front.controller;

import com.example.Ecommerce_Muebleria.Front.services.CartService;
import com.example.Ecommerce_Muebleria.Front.services.ProductService;
import com.example.Ecommerce_Muebleria.Front.services.internal.WishlistService;
import com.example.Ecommerce_Muebleria.entities.cart.Cart;
import com.example.Ecommerce_Muebleria.entities.cart.CartItem;
import com.example.Ecommerce_Muebleria.entities.commons.Product;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
public class WishlistController {
    @Autowired
    private WishlistService wishlistService;
    @Autowired private ProductService productService; // El que ya tenías

    // Asegúrate de inyectar CartService arriba en tu clase
    @Autowired
    private CartService cartService;

    @GetMapping("/wishlist")
    public String showWishlist(@AuthenticationPrincipal OidcUser oidcUser, Model model, HttpSession session) {

        String cartId = getCartId(oidcUser, session);

        if (oidcUser == null) {
            return "redirect:/oauth2/authorization/auth0";
        }

        // 2. Obtener favoritos (CAMBIO: Ya no pasamos userId)
        // El servicio usa el token para traer "mis" favoritos del microservicio
        List<Long> ids = wishlistService.getFavoriteIds();

        List<Product> favorites = ids.stream()
                .map(id -> productService.findProductById(id))
                .filter(Objects::nonNull)
                .toList();

        model.addAttribute("favoriteProducts", favorites);

        // 3. Lógica del Carrito (Para los botones +/- y el contador)
        Map<Long, Integer> productQuantities = new HashMap<>();
        try {
            Cart cart = cartService.getCart(cartId);
            if (cart != null && cart.getItems() != null) {
                for (CartItem item : cart.getItems()) {
                    Long pId = (item.getProductDetail() != null)
                            ? item.getProductDetail().getId()
                            : item.getProductId();

                    if (pId != null) {
                        productQuantities.put(pId, item.getQuantity());
                    }
                }
                model.addAttribute("cartCount", cart.getItems().size());
            }
        } catch (Exception e) {
            System.err.println("❌ Error cargando carrito en Wishlist: " + e.getMessage());
        }

        // 4. Pasar los datos al Modelo
        model.addAttribute("productQuantities", productQuantities);
        model.addAttribute("favoritesIds", ids);
        model.addAttribute("isWishlistPage", true);

        return "wishlist";
    }

    @GetMapping("/toggle-favorite/{productId}")
    public String toggleFavorite(
            @PathVariable Long productId,
            @AuthenticationPrincipal OidcUser oidcUser,
            Model model) {

        if (oidcUser == null) {
            model.addAttribute("isFavorite", false);
            model.addAttribute("productId", productId);
            return "shop :: btnFavorito";
        }

        try {
            // --- CAMBIO CLAVE ---
            // Ya no mandamos 'userId'. El token va por "atrás" en los Headers.
            boolean newState = wishlistService.toggleFavorite(productId);

            model.addAttribute("isFavorite", newState);
            model.addAttribute("productId", productId);

        } catch (Exception e) {
            System.err.println("❌ Error en favorito: " + e.getMessage());
            model.addAttribute("isFavorite", false);
        }

        return "shop :: btnFavorito";
    }

    private String getCartId(OidcUser oidcUser, HttpSession session) {
        if (oidcUser != null) {
            return oidcUser.getSubject(); // Usuario real de Auth0
        }
        // Si no hay login, usamos el ID de la sesión del navegador
        return "GUEST_" + session.getId();
    }
}