package com.marinboy.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Firebase가 활성화된 환경에서만 Admin SDK를 초기화합니다. */
@Configuration
@ConditionalOnProperty(name = "app.firebase.enabled", havingValue = "true")
public class FirebaseConfig {

    @Bean
    FirebaseApp firebaseApp(
            @Value("${app.firebase.project-id}") String projectId,
            @Value("${app.firebase.credentials-path:}") String credentialsPath
    ) throws IOException {
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException("FIREBASE_PROJECT_ID가 필요합니다.");
        }

        GoogleCredentials credentials;
        if (credentialsPath == null || credentialsPath.isBlank()) {
            credentials = GoogleCredentials.getApplicationDefault();
        } else {
            try (InputStream input = Files.newInputStream(Path.of(credentialsPath))) {
                credentials = GoogleCredentials.fromStream(input);
            }
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build();
        return FirebaseApp.getApps().stream()
                .findFirst()
                .orElseGet(() -> FirebaseApp.initializeApp(options));
    }

    @Bean
    FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
        return FirebaseAuth.getInstance(firebaseApp);
    }
}
