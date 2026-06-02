package com.example.Ecommerce_Muebleria.BackProducts.repositories;


import com.example.Ecommerce_Muebleria.entities.commons.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {

    Optional<Collection> findById(Long id);
    List<Collection> findByActiveTrue();
}