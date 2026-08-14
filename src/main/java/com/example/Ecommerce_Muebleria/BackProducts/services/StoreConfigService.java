package com.example.Ecommerce_Muebleria.BackProducts.services;

import com.example.Ecommerce_Muebleria.BackProducts.repositories.StoreConfigRepository;
import com.example.Ecommerce_Muebleria.entities.front.StoreConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreConfigService {

    @Autowired
    private StoreConfigRepository repository;

    // Obtiene la configuración (si no existe, la crea)
    public StoreConfig getConfig() {
        return repository.findById(1L).orElseGet(() -> {
            StoreConfig newConfig = new StoreConfig();
            newConfig.setId(1L);
            newConfig.setBannerActive(true);
            newConfig.setCarouselActive(true);
            newConfig.setCollectionsActive(true);
            return repository.save(newConfig);
        });
    }

    // Métodos para alternar (apagar/prender) cada sección
    public void toggleBanner() {
        StoreConfig config = getConfig();
        config.setBannerActive(!config.isBannerActive());
        repository.save(config);
    }

    public void toggleCarousel() {
        StoreConfig config = getConfig();
        config.setCarouselActive(!config.isCarouselActive());
        repository.save(config);
    }

    public void toggleCollections() {
        StoreConfig config = getConfig();
        config.setCollectionsActive(!config.isCollectionsActive());
        repository.save(config);
    }
}