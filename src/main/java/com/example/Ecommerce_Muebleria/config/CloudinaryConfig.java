package com.example.Ecommerce_Muebleria.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {
        @Value("${cloudinary.cloud-name}")
        private String cloudName;

        @Value("${cloudinary.api-key}")
        private String apiKey;

        @Value("${cloudinary.api-secret}")
        private String apiSecret;

        @Bean
        public Cloudinary cloudinary() {
            // Log preventivo para que veas en la consola si realmente cargó algo
            // (No loguees el secret en producción, esto es solo para debug)
            System.out.println("☁️ Configurando Cloudinary con Cloud Name: " + cloudName);

            return new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret
            ));
        }
    }