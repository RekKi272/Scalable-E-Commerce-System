package com.hmkeyewear.cart_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.cart_service.dto.DiscountRequestDto;
import com.hmkeyewear.cart_service.dto.DiscountResponseDto;
import com.hmkeyewear.cart_service.mapper.DiscountMapper;
import com.hmkeyewear.cart_service.messaging.DiscountEventProducer;
import com.hmkeyewear.cart_service.model.Discount;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class DiscountService {
    private static final String COLLECTION_NAME = "discounts";

    private final DiscountMapper discountMapper;
    private final DiscountEventProducer discountEventProducer;

    public DiscountService(DiscountEventProducer discountEventProducer, DiscountMapper discountMapper) {
        this.discountEventProducer = discountEventProducer;
        this.discountMapper = discountMapper;
    }

    // CREATE Discount
    public DiscountResponseDto createDiscount(DiscountRequestDto discountRequestDto, String createBy) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        Discount discount = discountMapper.toDiscount(discountRequestDto);

        discount.setCreatedBy(createBy);
        discount.setCreatedAt(Timestamp.now());

        ApiFuture<WriteResult> result = docRef.set(discount);
        result.get();

        // Send message to RabbitMQ
        discountEventProducer.sendMessage(discount);

        return discountMapper.toDiscountResponseDto(discount);
    }

    // READ Discount
    public DiscountResponseDto getDiscountById(String discountId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(discountId);

        DocumentSnapshot document = docRef.get().get();
        if (!document.exists()) {
            return null;
        }

        Discount discount = document.toObject(Discount.class);
        return discountMapper.toDiscountResponseDto(discount);
    }

    // READ ALL
    public List<DiscountResponseDto> getAllDiscounts() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> documents = db.collection(COLLECTION_NAME).get().get().getDocuments();
        
        List<DiscountResponseDto> discountResponseDtos = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            Discount discount = document.toObject(Discount.class);
            discountResponseDtos.add(discountMapper.toDiscountResponseDto(discount));
        }
        return discountResponseDtos;
    }

    // READ ALL RAW
    public List<Discount> getAllDiscountsRaw() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> documents = db.collection(COLLECTION_NAME).get().get().getDocuments();
        List<Discount> discountList = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            Discount discount = document.toObject(Discount.class);
            discountList.add(discount);
        }
        return discountList;
    }

    // UPDATE Discount
    public DiscountResponseDto updateDiscount(String userId, DiscountRequestDto discountRequestDto) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(discountRequestDto.getDiscountId());

        DocumentSnapshot snapshot = docRef.get().get();

        if (!snapshot.exists()) {
            throw new RuntimeException("Discount with ID " + discountRequestDto.getDiscountId() + " not found");
        }

        Discount existingDiscount = snapshot.toObject(Discount.class);
        if (existingDiscount == null) {
            throw new RuntimeException("Discount with ID " + discountRequestDto.getDiscountId() + " not found");
        }
        Discount discount = discountMapper.toDiscount(discountRequestDto);
        discount.setCreatedAt(existingDiscount.getCreatedAt());
        discount.setCreatedBy(existingDiscount.getCreatedBy());
        discount.setDiscountId(existingDiscount.getDiscountId());
        discount.setUpdatedAt(Timestamp.now());
        discount.setUpdatedBy(userId);

        // Send message to RabbitMQ
        discountEventProducer.sendMessage(discount);

        return discountMapper.toDiscountResponseDto(discount);
    }

    // DELETE Discount
    public String deleteDiscount(String discountId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(discountId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();

        discountEventProducer.sendMessage(discountId);

        return "Discount " + discountId + " is deleted";
    }

    // UPDATE Usage
    public void updateDiscountUsage(Discount discount) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("discounts").document(discount.getDiscountId());
        docRef.update("usedQuantity", discount.getUsedQuantity()).get();

        // Gửi message update tới RabbitMQ nếu cần
        discountEventProducer.sendMessage(discount);
    }

}
