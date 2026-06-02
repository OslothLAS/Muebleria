package com.example.Ecommerce_Muebleria.Front.services.internal;

import com.example.Ecommerce_Muebleria.BackProducts.repositories.WishlistRepository;
import com.example.Ecommerce_Muebleria.entities.products.WishlistItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistItemRepository;

    // 🚀 MÉTODO MAGICO: Extrae el ID de Auth0 directamente de la memoria de Spring
    private String getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof OidcUser oidcUser) {
            return oidcUser.getSubject(); // Este es el ID real de Auth0
        }
        return null; // No está logueado
    }

    // 1. ALTERNAR FAVORITO (Vuelve a tu firma original)
    public boolean toggleFavorite(Long productId) {
        String userId = getAuthenticatedUserId();
        if (userId == null) return false;

        try {
            Optional<WishlistItem> existingFavorite = wishlistItemRepository.findByUserIdAndProductId(userId, productId);

            if (existingFavorite.isPresent()) {
                wishlistItemRepository.delete(existingFavorite.get());
                log.info("💔 Producto {} eliminado de favoritos", productId);
                return false;
            } else {
                WishlistItem newFavorite = new WishlistItem(userId, productId);
                wishlistItemRepository.save(newFavorite);
                log.info("❤️ Producto {} agregado a favoritos", productId);
                return true;
            }
        } catch (Exception e) {
            log.error("❌ Error en toggleFavorite local: {}", e.getMessage());
            return false;
        }
    }

    // 2. OBTENER TODOS LOS IDs (Vuelve a tu firma original)
    public List<Long> getFavoriteIds() {
        String userId = getAuthenticatedUserId();
        if (userId == null) return new ArrayList<>();

        try {
            return wishlistItemRepository.findByUserId(userId).stream()
                    .map(WishlistItem::getProductId)
                    .toList();
        } catch (Exception e) {
            log.error("❌ Error trayendo IDs de favoritos locales: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // 3. VERIFICAR SI UN PRODUCTO ES FAVORITO (Dejamos esta por si la usás en detalles)
    public boolean isFavorite(Long productId) {
        String userId = getAuthenticatedUserId();
        if (userId == null || productId == null) return false;

        try {
            return wishlistItemRepository.existsByUserIdAndProductId(userId, productId);
        } catch (Exception e) {
            log.error("❌ Error en isFavorite check local: {}", e.getMessage());
            return false;
        }
    }

    // Sobrecarga por si en algún lado sí pasabas el userId explícitamente
    public boolean isFavorite(String userId, Long productId) {
        if (userId == null || productId == null) return false;
        return wishlistItemRepository.existsByUserIdAndProductId(userId, productId);
    }
}