package com.hmkeyewear.product_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.product_service.dto.CategoryRequestDto;
import com.hmkeyewear.product_service.dto.CategoryResponseDto;
import com.hmkeyewear.product_service.mapper.CategoryMapper;
import com.hmkeyewear.product_service.messaging.CategoryEventProducer;
import com.hmkeyewear.product_service.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    private final CategoryEventProducer categoryEventProducer;

    private static final String COLLECTION_NAME = "categories";
    private static final String COUNTER_COLLECTION_NAME = "counters";
    private static final String CATEGORY_COUNTER_DOC = "categoryCounter";

    // Constructor
    public CategoryService(CategoryMapper categoryMapper, CategoryEventProducer categoryEventProducer) {
        this.categoryMapper = categoryMapper;
        this.categoryEventProducer = categoryEventProducer;
    }

    // Generate CategoryId
    private String generateCategoryId(Firestore db) throws ExecutionException, InterruptedException {
        DocumentReference counterRef = db.collection(COUNTER_COLLECTION_NAME).document(CATEGORY_COUNTER_DOC);

        ApiFuture<String> future = db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef).get();

            Long lastIdObj = (snapshot.exists() && snapshot.contains("lastId")) ? snapshot.getLong("lastId") : null;
            long lastId = (lastIdObj != null) ? lastIdObj : 0;

            long newId = lastId + 1;
            // set với merge để tạo document nếu chưa tồn tại
            transaction.set(counterRef, Map.of("lastId", newId), SetOptions.merge());

            // --- fix lỗi dữ chuỗi ---
            String formattedId = String.format("%03d", newId);
            if (formattedId.length() > 3)
                formattedId = String.valueOf(newId); // giữ nguyên nếu vượt 999

            return "CAT" + formattedId;
        });
        return future.get();
    }

    // CREATE Category
    public CategoryResponseDto createCategory(CategoryRequestDto dto, String createdBy) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        String newCategoryId = generateCategoryId(db);

        Category category = new Category();
        category.setCategoryId(newCategoryId);
        category.setCreatedBy(createdBy);
        category.setCategoryName(dto.getCategoryName());
        category.setCreatedAt(Timestamp.now());

        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(category.getCategoryId())
                .set(category);

        // --- Send message to RabbitMQ ---
        categoryEventProducer.sendMessage(category);

        return categoryMapper.toCategoryResponseDto(category);
    }

    // READ ONE Category
    public CategoryResponseDto getCategoryById(String categoryId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference categoryRef = db.collection(COLLECTION_NAME).document(categoryId);
        ApiFuture<DocumentSnapshot> future = categoryRef.get();
        DocumentSnapshot snapshot = future.get();

        if (snapshot.exists()) {
            Category category = snapshot.toObject(Category.class);
            return categoryMapper.toCategoryResponseDto(category);
        }
        return null;
    }

    // READ ALL Categories
    public List<CategoryResponseDto> getAllCategories() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<CategoryResponseDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Category category = doc.toObject(Category.class);
            result.add(categoryMapper.toCategoryResponseDto(category));
        }
        return result;
    }

    // UPDATE Category
    public CategoryResponseDto updateCategory(String categoryId, CategoryRequestDto dto, String updatedBy) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference categoryRef = db.collection(COLLECTION_NAME).document(categoryId);

        ApiFuture<DocumentSnapshot> future = categoryRef.get();
        DocumentSnapshot document = future.get();
        if (!document.exists()) {
            throw new RuntimeException("Category with ID " + categoryId + " not found");
        }

        Category existingCategory = document.toObject(Category.class);

        if (existingCategory == null) {
            throw new RuntimeException("Category data is null for ID " + categoryId);
        }

        Category updatedCategory = categoryMapper.toCategory(dto);

        updatedCategory.setCategoryId(categoryId);
        updatedCategory.setCreatedAt(existingCategory.getCreatedAt());
        updatedCategory.setCreatedBy(existingCategory.getCreatedBy());


        ApiFuture<WriteResult> writeResult = categoryRef.set(updatedCategory);
        writeResult.get();

        // --- Send message to RabbitMQ ---
        categoryEventProducer.sendMessage(updatedCategory);

        return categoryMapper.toCategoryResponseDto(updatedCategory);
    }

    // DELETE Category
    public String deleteCategory(String categoryId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(categoryId)
                .delete();
        future.get();

        // --- Send message to RabbitMQ ---
        categoryEventProducer.sendMessage(categoryId);
        return "Successfully deleted category with id " + categoryId;
    }
}
