package com.example.Ecommerce_Muebleria.BackProducts.repositories;


import com.example.Ecommerce_Muebleria.entities.commons.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByIsNewTrue();
    List<Product> findByEsDestacadoTrue();
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    boolean existsById(long id);
    void deleteById(long id);
    Optional<Product> findById(Long id);
    void deleteByActivoFalse();
    List<Product> findByActivoTrue();
    Page<Product> findByActivoTrue(Pageable pageable);
    Page<Product> findByNameContainingIgnoreCaseAndActivoTrue(String name, Pageable pageable);

    List<Product> findByIsNewTrueAndActivoTrue();
    List<Product> findByEsDestacadoTrueAndActivoTrue();
    // Si quieres buscar por nombre O descripción:
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    @Modifying
    @Query("UPDATE Product p SET p.isNew = false WHERE p.isNew = true AND p.createdAt < :cutoffDate")
    int removeNewStatusFromOldProducts(@Param("cutoffDate") LocalDateTime cutoffDate);
}
