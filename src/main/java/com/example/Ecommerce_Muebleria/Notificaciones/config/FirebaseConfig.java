package com.example.Ecommerce_Muebleria.Notificaciones.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() {
        try {
            InputStream serviceAccount = getClass().getResourceAsStream("/serviceAccountKey.json");

            if (serviceAccount == null) {
                throw new RuntimeException("No se encontró serviceAccountKey.json");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Evitamos inicializarlo dos veces si se recarga el contexto
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("🔥 Firebase Admin SDK inicializado correctamente.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}