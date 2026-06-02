package com.example.Ecommerce_Muebleria.BackCartOrder.repositories;



import com.example.Ecommerce_Muebleria.entities.cart.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(String userId);
    Optional<Order> findById(Long id);
}