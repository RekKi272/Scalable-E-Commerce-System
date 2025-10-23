package com.hmkeyewear.blog_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.blog_service.dto.BannerRequestDto;
import com.hmkeyewear.blog_service.dto.BannerResponseDto;
import com.hmkeyewear.blog_service.mapper.BannerMapper;
import com.hmkeyewear.blog_service.model.Banner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    private static final String COLLECTION_NAME = "banners";
    private static final String COUNTER_COLLECTION = "counters";
    private static final String BANNER_COUNTER_DOC = "bannerCounter";

    private String generateBannerId(Firestore db) throws ExecutionException, InterruptedException {
        DocumentReference counterRef = db.collection(COUNTER_COLLECTION).document(BANNER_COUNTER_DOC);

        ApiFuture<String> future = db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef).get();
            long lastId = snapshot.exists() && snapshot.contains("lastId") ? snapshot.getLong("lastId") : 0;
            long newId = lastId + 1;
            transaction.set(counterRef, Map.of("lastId", newId), SetOptions.merge());
            return String.format("BANNER%04d", newId);
        });

        return future.get();
    }

    public BannerResponseDto createBanner(String createdBy, BannerRequestDto dto)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        Banner banner = bannerMapper.toBanner(dto);
        banner.setBannerId(generateBannerId(db));
        banner.setCreatedAt(Timestamp.now());
        banner.setCreatedBy(createdBy);

        db.collection(COLLECTION_NAME).document(banner.getBannerId()).set(banner).get();

        return bannerMapper.toBannerResponseDto(banner);
    }

    public List<BannerResponseDto> getAllBanners() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> documents = db.collection(COLLECTION_NAME).get().get().getDocuments();

        List<BannerResponseDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Banner banner = doc.toObject(Banner.class);
            result.add(bannerMapper.toBannerResponseDto(banner));
        }

        // Giữ sort trong backend (nếu muốn theo updatedAt)
        result.sort((a, b) -> {
            Timestamp aTime = a.getUpdatedAt() != null ? a.getUpdatedAt() : a.getCreatedAt();
            Timestamp bTime = b.getUpdatedAt() != null ? b.getUpdatedAt() : b.getCreatedAt();

            double aSeconds = aTime != null ? aTime.getSeconds() + aTime.getNanos() / 1e9 : 0;
            double bSeconds = bTime != null ? bTime.getSeconds() + bTime.getNanos() / 1e9 : 0;

            return Double.compare(bSeconds, aSeconds);
        });

        return result;
    }

    public BannerResponseDto getBannerById(String bannerId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(bannerId).get().get();

        if (!snapshot.exists())
            return null;

        return bannerMapper.toBannerResponseDto(snapshot.toObject(Banner.class));
    }

    public BannerResponseDto updateBanner(String bannerId, BannerRequestDto dto, String updatedBy)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(bannerId);
        DocumentSnapshot snapshot = docRef.get().get();

        if (!snapshot.exists())
            return null;

        Banner banner = snapshot.toObject(Banner.class);
        if (banner == null)
            return null;

        banner.setTitle(dto.getTitle());
        banner.setImageBase64(dto.getImageBase64());
        banner.setStatus(dto.getStatus());
        banner.setUpdatedBy(updatedBy);
        banner.setUpdatedAt(Timestamp.now());

        docRef.set(banner).get();
        return bannerMapper.toBannerResponseDto(banner);
    }

    public String deleteBanner(String bannerId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(bannerId).delete().get();
        return "Banner deleted successfully: " + bannerId;
    }

    // ✅ Chỉ trả về list banner active, không sort — FE tự sort
    public List<BannerResponseDto> getAllActiveBanners() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> documents = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .get()
                .getDocuments();

        List<BannerResponseDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Banner banner = doc.toObject(Banner.class);
            result.add(bannerMapper.toBannerResponseDto(banner));
        }

        return result;
    }
}
