package com.example.Ecommerce_Muebleria.BackCartOrder.clients;

import com.example.Ecommerce_Muebleria.entities.commons.Product;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


public interface ProductClient {

    @GetMapping("/api/products/{id}")
    Product getProductById(@PathVariable("id") Long id);
}