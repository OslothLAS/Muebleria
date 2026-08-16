package com.example.Ecommerce_Muebleria.BackProducts.repositories;


import com.example.Ecommerce_Muebleria.entities.commons.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNewProductTrue();
    List<Product> findByEsDestacadoTrue();
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    boolean existsById(long id);
    void deleteById(long id);
    Optional<Product> findById(Long id);
    void deleteByActivoFalse();
    List<Product> findByActivoTrue();
    Page<Product> findByActivoTrue(Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndActivoTrue(String name, Pageable pageable);
    List<Product> findByNewProductTrueAndActivoTrue();
    List<Product> findByEsDestacadoTrueAndActivoTrue();
    // Si quieres buscar por nombre O descripción:
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    @Modifying
    @Query("UPDATE Product p SET p.newProduct = false WHERE p.newProduct = true AND p.createdAt < :cutoffDate")
    int removeNewStatusFromOldProducts(@Param("cutoffDate") LocalDateTime cutoffDate);

    // 1. Para buscar productos de UNA categoría específica (ej. para el HomeController)
    List<Product> findByCategoriesContainingAndActivoTrue(String category);

    // 2. Para buscar productos recomendados que compartan AL MENOS UNA categoría con el producto actual
    List<Product> findDistinctByCategoriesInAndActivoTrue(List<String> categories, Pageable pageable);
}
