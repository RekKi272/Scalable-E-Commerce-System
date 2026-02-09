package com.hmkeyewear.product_service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
@RequiredArgsConstructor
@EnableScheduling
@EnableElasticsearchRepositories(
		basePackages = "com.hmkeyewear.product_service.repository"
)
public class ProductServiceApplication{

	public static void main(String[] args) throws IOException {
		initFirebase();
		SpringApplication.run(ProductServiceApplication.class, args);
	}

	private static void initFirebase() {
		try {
			String credentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

			if (credentials == null || credentials.isBlank()) {
				throw new IllegalStateException("Missing GOOGLE_APPLICATION_CREDENTIALS");
			}

			InputStream serviceAccount;

			// Nếu là path file
			if (credentials.endsWith(".json")) {
				serviceAccount = new FileInputStream(credentials);
			}
			// Nếu là JSON string
			else {
				serviceAccount = new ByteArrayInputStream(
						credentials.getBytes(StandardCharsets.UTF_8)
				);
			}

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize Firebase", e);
		}
	}

}
