package com.example.Ecommerce_Muebleria.BackProducts.repositories;


import com.example.Ecommerce_Muebleria.entities.products.WishlistItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    Optional<WishlistItem> findByUserIdAndProductId(String userId, Long productId);
    List<WishlistItem> findByUserId(String userId);
    @Modifying
    @Transactional
    void deleteByUserIdAndProductId(String userId, Long productId);
    boolean existsByUserIdAndProductId(String userId, Long productId);
}