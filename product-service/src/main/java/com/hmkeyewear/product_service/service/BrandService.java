package com.hmkeyewear.product_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.product_service.dto.BrandRequestDto;
import com.hmkeyewear.product_service.dto.BrandResponseDto;
import com.hmkeyewear.product_service.mapper.BrandMapper;
import com.hmkeyewear.product_service.messaging.BrandEventProducer;
import com.hmkeyewear.product_service.model.Brand;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class BrandService {

    private final BrandMapper brandMapper;

    private final BrandEventProducer brandEventProducer;

    private static final String COLLECTION_NAME = "brands";
    private static final String COUNTER_COLLECTION = "counters";
    private static final String BRAND_COUNTER_DOC = "brandCounter";

    // Constructor
    public BrandService(BrandMapper brandMapper, BrandEventProducer brandEventProducer) {
        this.brandMapper = brandMapper;
        this.brandEventProducer = brandEventProducer;
    }

    // Generate BrandId
    private String generateBrandId(Firestore db) throws ExecutionException, InterruptedException {
        DocumentReference counterRef = db.collection(COUNTER_COLLECTION).document(BRAND_COUNTER_DOC);

        ApiFuture<String> future = db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef).get();

            Long lastIdObj = (snapshot.exists() && snapshot.contains("lastId")) ? snapshot.getLong("lastId") : null;
            long lastId = (lastIdObj != null) ? lastIdObj : 0;

            long newId = lastId + 1;
            // dùng set với merge để tạo document nếu chưa tồn tại
            transaction.set(counterRef, Map.of("lastId", newId), SetOptions.merge());

            // --- fix lỗi dữ chuỗi ---
            String formattedId = String.format("%03d", newId);
            if (formattedId.length() > 3)
                formattedId = String.valueOf(newId); // giữ nguyên nếu vượt 999

            return "BRAND" + formattedId;
        });

        return future.get();
    }

    // CREATE Brand
    public BrandResponseDto createBrand(BrandRequestDto dto, String createdBy)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        String newBrandId = generateBrandId(db);

        Brand brand = new Brand();
        brand.setBrandId(newBrandId);
        brand.setCreatedBy(createdBy);
        brand.setBrandName(dto.getBrandName());
        brand.setCreatedAt(Timestamp.now());

        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(brand.getBrandId())
                .set(brand);

        future.get();

        // --- Send message to RabbitMQ ---
        brandEventProducer.sendMessage(brand);

        return brandMapper.toBrandResponseDto(brand);
    }

    // READ ONE Brand
    public BrandResponseDto getBrandById(String brandId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference brandRef = db.collection(COLLECTION_NAME).document(brandId);
        ApiFuture<DocumentSnapshot> future = brandRef.get();
        DocumentSnapshot snapshot = future.get();

        if (snapshot.exists()) {
            Brand brand = snapshot.toObject(Brand.class);
            return brandMapper.toBrandResponseDto(brand);
        }
        return null;
    }

    // READ ALL Brands
    public List<BrandResponseDto> getAllBrands() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<BrandResponseDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Brand brand = doc.toObject(Brand.class);
            result.add(brandMapper.toBrandResponseDto(brand));
        }
        return result;
    }

    // UPDATE Brand
    public BrandResponseDto updateBrand(String brandId, BrandRequestDto dto, String updatedBy)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference brandRef = db.collection(COLLECTION_NAME).document(brandId);

        ApiFuture<DocumentSnapshot> future = brandRef.get();
        DocumentSnapshot document = future.get();
        if (!document.exists()) {
            throw new RuntimeException("Brand with ID " + brandId + " not found");
        }

        Brand existingBrand = document.toObject(Brand.class);

        if (existingBrand == null) {
            throw new RuntimeException("Brand data is null for ID " + brandId);
        }

        Brand updatedBrand = brandMapper.toBrand(dto);

        updatedBrand.setBrandId(brandId);
        updatedBrand.setCreatedAt(existingBrand.getCreatedAt());
        updatedBrand.setCreatedBy(existingBrand.getCreatedBy());

        ApiFuture<WriteResult> writeResult = brandRef.set(updatedBrand);
        writeResult.get();

        // --- Send message to RabbitMQ ---
        brandEventProducer.sendMessage(updatedBrand);

        return brandMapper.toBrandResponseDto(updatedBrand);
    }

    // DELETE Brand
    public String deleteBrand(String brandId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(brandId)
                .delete();
        future.get();

        // --- Send message to RabbitMQ ---
        brandEventProducer.sendMessage(brandId);
        return "Successfully deleted brand with id " + brandId;
    }
}
