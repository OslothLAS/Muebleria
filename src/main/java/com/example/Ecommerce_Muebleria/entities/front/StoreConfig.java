package com.example.Ecommerce_Muebleria.entities.front;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreConfig {

    @Id
    private Long id = 1L; // Siempre usaremos el ID 1

    private boolean bannerActive = true;
    private boolean carouselActive = true;
    private boolean collectionsActive = true;
}