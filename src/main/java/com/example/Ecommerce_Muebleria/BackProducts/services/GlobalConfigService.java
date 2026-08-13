package com.example.Ecommerce_Muebleria.BackProducts.services;


import com.example.Ecommerce_Muebleria.BackProducts.repositories.GlobalConfigRepository;
import com.example.Ecommerce_Muebleria.config.GlobalConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GlobalConfigService {

    @Autowired
    private GlobalConfigRepository configRepository;

    // Obtiene la configuración (y si no existe, la crea en el momento)
    public GlobalConfig getConfig() {
        return configRepository.findById(1L).orElseGet(() -> {
            GlobalConfig newConfig = new GlobalConfig();
            newConfig.setId(1L);
            return configRepository.save(newConfig);
        });
    }

    // Actualiza la URL del banner
    public void updateBannerUrl(String url) {
        GlobalConfig config = getConfig();
        config.setSuperBannerUrl(url);
        configRepository.save(config);
    }

    // Elimina el banner
    public void removeBannerUrl() {
        GlobalConfig config = getConfig();
        config.setSuperBannerUrl(null);
        configRepository.save(config);
    }
}