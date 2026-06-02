package com.example.Ecommerce_Muebleria.config;

import org.apache.catalina.Context;

import org.springframework.boot.tomcat.TomcatContextCustomizer;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomCatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addContextCustomizers(new TomcatContextCustomizer() {
            @Override
            public void customize(Context context) {
                // Esto es la orden directa a Tomcat para que relaje las reglas
                // del parseo multipart y permita múltiples archivos y campos.
                context.setAllowCasualMultipartParsing(true);
            }
        });
    }
}