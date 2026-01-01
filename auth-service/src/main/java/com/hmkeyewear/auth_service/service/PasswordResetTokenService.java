package com.hmkeyewear.auth_service.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.auth_service.model.PasswordResetToken;
import com.hmkeyewear.auth_service.model.User;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
public class PasswordResetTokenService {
    private static final String COLLECTION_NAME = "password_reset_tokens";
    private static final int EXPIRE_MINUTES = 15;

    private final AuthService authService;

    /* ========== HASH TOKEN ========== */
    private String hash(String token) {
        return DigestUtils.sha256Hex(token);
    }

    /* ========== PROCESSING FORGOT PASSWORD ========== */
    private void processForgotPassword(String email) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Get User
        Optional<User> optionalUser;
        try {
            optionalUser = authService.findByEmail(email);
        } catch (Exception e) {
            return; // always return OK
        }

        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();

        // Generate token

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hash(rawToken);

        Instant expiryInstant = Instant.now()
                .plus(EXPIRE_MINUTES, ChronoUnit.MINUTES);

        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .id(docRef.getId())
                .userId(user.getUserId())
                .tokenHash(tokenHash)
                .expiryTime(Timestamp.ofTimeSecondsAndNanos(
                        expiryInstant.getEpochSecond(), 0))
                .used(false)
                .createdAt(Timestamp.now())
                .build();

        docRef.set(passwordResetToken).get();
    }
}
