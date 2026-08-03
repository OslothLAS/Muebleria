package com.example.Ecommerce_Muebleria.BackCartOrder.repositories;

import com.example.Ecommerce_Muebleria.entities.cart.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Busca qué otros productos estaban en la misma orden que el producto actual
    // Los agrupa y los ordena de mayor a menor según la cantidad de veces que se compraron juntos.
    @Query("SELECT oi2.productId FROM OrderItem oi1 " +
            "JOIN oi1.order o " +
            "JOIN o.orderItems oi2 " +
            "WHERE oi1.productId = :productId AND oi2.productId != :productId " +
            "GROUP BY oi2.productId " +
            "ORDER BY COUNT(oi2.productId) DESC")
    List<Long> findFrequentlyBoughtTogether(@Param("productId") Long productId, Pageable pageable);
}