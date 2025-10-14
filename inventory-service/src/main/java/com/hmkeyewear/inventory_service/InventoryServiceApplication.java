package com.hmkeyewear.inventory_service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootApplication
@EnableFeignClients
public class InventoryServiceApplication {

    public static void main(String[] args) throws IOException {
        String credentialPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credentialPath == null || credentialPath.isEmpty()) {
            throw new IllegalStateException("Missing GOOGLE_APPLICATION_CREDENTIALS environment variable");
        }

        try (FileInputStream serviceAccount = new FileInputStream(credentialPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        }

        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
