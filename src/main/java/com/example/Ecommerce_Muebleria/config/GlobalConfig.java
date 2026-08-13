package com.example.Ecommerce_Muebleria.config;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "global_config")
public class GlobalConfig {

    @Id
    private Long id = 1L; // Siempre será 1 porque es configuración única

    private String superBannerUrl;

    // Constructor vacío requerido por JPA
    public GlobalConfig() {}

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSuperBannerUrl() {
        return superBannerUrl;
    }



    public void setSuperBannerUrl(String superBannerUrl) {
        this.superBannerUrl = superBannerUrl;
    }
}