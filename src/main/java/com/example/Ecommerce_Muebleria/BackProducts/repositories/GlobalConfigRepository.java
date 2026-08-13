package com.example.Ecommerce_Muebleria.BackProducts.repositories;


import com.example.Ecommerce_Muebleria.config.GlobalConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalConfigRepository extends JpaRepository<GlobalConfig, Long> {
}