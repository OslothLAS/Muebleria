package com.example.Ecommerce_Muebleria.Notificaciones.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    // 🐳 En Docker se monta el archivo y se pasa su ruta (FIREBASE_CREDENTIALS_PATH),
    // así la clave no queda guardada dentro de la imagen.
    @Value("${app.firebase.credentials-path:}")
    private String credentialsPath;

    @PostConstruct
    public void initFirebase() {
        try (InputStream serviceAccount = openCredentials()) {

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

    private InputStream openCredentials() throws Exception {
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            return new FileInputStream(credentialsPath);
        }
        // Ejecución local (IntelliJ): src/main/resources/serviceAccountKey.json
        return getClass().getResourceAsStream("/serviceAccountKey.json");
    }
}
