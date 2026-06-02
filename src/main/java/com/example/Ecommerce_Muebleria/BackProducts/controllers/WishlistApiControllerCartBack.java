package com.example.Ecommerce_Muebleria.BackProducts.controllers;


import com.example.Ecommerce_Muebleria.entities.products.WishlistItem;
import com.example.Ecommerce_Muebleria.BackProducts.repositories.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistApiControllerCartBack {

    @Autowired
    private WishlistRepository wishlistRepository;

    @PostMapping("/toggle")
    public boolean toggle(@AuthenticationPrincipal Jwt jwt, @RequestParam Long productId) {
        // El ID viene limpio del token ("sub"), sin errores de %7C
        String userId = jwt.getSubject();

        Optional<WishlistItem> fav = wishlistRepository.findByUserIdAndProductId(userId, productId);
        if (fav.isPresent()) {
            wishlistRepository.deleteByUserIdAndProductId(userId, productId);
            return false;
        } else {
            wishlistRepository.save(new WishlistItem(userId, productId));
            return true;
        }
    }

    @GetMapping("/check")
    public boolean isFavorite(@AuthenticationPrincipal Jwt jwt, @RequestParam Long productId) {
        String userId = jwt.getSubject();
        return wishlistRepository.findByUserIdAndProductId(userId, productId).isPresent();
    }

    @GetMapping("/user/{userId}")
    public List<Long> getFavoriteIds(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return wishlistRepository.findByUserId(userId).stream()
                .map(WishlistItem::getProductId).toList();
    }

    @GetMapping("/mine") // Coincide con el path del WebClient
    public List<Long> getMyFavoriteIds(@AuthenticationPrincipal Jwt jwt) {
        // Sacamos el ID limpio directamente del token
        String userId = jwt.getSubject();

        // Tu lógica de base de datos de siempre
        return wishlistRepository.findByUserId(userId).stream()
                .map(WishlistItem::getProductId).toList();
    }
}