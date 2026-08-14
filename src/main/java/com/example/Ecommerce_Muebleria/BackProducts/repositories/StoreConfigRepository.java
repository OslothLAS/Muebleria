package com.example.Ecommerce_Muebleria.BackProducts.repositories;

import com.example.Ecommerce_Muebleria.entities.front.StoreConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreConfigRepository extends JpaRepository<StoreConfig, Long> {
}