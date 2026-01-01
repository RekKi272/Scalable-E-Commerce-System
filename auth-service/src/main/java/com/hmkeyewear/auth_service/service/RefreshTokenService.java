package com.hmkeyewear.auth_service.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.auth_service.model.RefreshToken;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class RefreshTokenService {

    private static final String COLLECTION_NAME = "refresh_tokens";
    private static final int EXPIRE_DAYS = 7;

    /* ========== HASH TOKEN ========== */
    private String hash(String token) {
        return DigestUtils.sha256Hex(token);
    }

    /* ========== LOGIN: revoke toàn bộ token cũ ========= */
    public void revokeAllByUser(String userId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        QuerySnapshot snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .whereEqualTo("revoked", false)
                .get()
                .get();

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            doc.getReference().update("revoked", true);
        }
    }

    /* ========== CREATE REFRESH TOKEN ========= */
    public String create(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hash(rawToken);

        Instant now = Instant.now();
        Instant expiry = now.plus(EXPIRE_DAYS, ChronoUnit.DAYS); // 7 Days last

        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(docRef.getId())
                .userId(userId)
                .tokenHash(tokenHash)
                .revoked(false)
                .expiresAt(Timestamp.ofTimeSecondsAndNanos(now.getEpochSecond(), 0 ))
                .expiresAt(Timestamp.ofTimeSecondsAndNanos(expiry.getEpochSecond(), 0))
                .build();

        docRef.set(refreshToken).get();

        return rawToken;
    }

    /* ========== VALIDATE REFRESH TOKEN ========= */
    public RefreshToken verify(String rawToken)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        String tokenHash = hash(rawToken);

        QuerySnapshot snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("tokenHash", tokenHash)
                .whereEqualTo("revoked", false)
                .get()
                .get();

        if (snapshot.isEmpty()) {
            throw new RuntimeException("Refresh token invalid");
        }

        RefreshToken token = snapshot.getDocuments().get(0).toObject(RefreshToken.class);

        if (token.getExpiresAt().toDate().before(Timestamp.now().toDate())) {
            throw new RuntimeException("Refresh token expired");
        }

        return token;
    }

    /* ========== LOGOUT ========= */
    public void logout(String rawToken)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        String tokenHash = hash(rawToken);

        QuerySnapshot snapshot = db.collection(COLLECTION_NAME)
                .whereEqualTo("tokenHash", tokenHash)
                .get()
                .get();

        if (!snapshot.isEmpty()) {
            snapshot.getDocuments()
                    .get(0)
                    .getReference()
                    .update("revoked", true);
        }
    }

}
