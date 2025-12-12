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

    // ====================================================================
    // HÀM CONVERT GIỮA STRING ↔ TIMESTAMP
    // ====================================================================
    private Timestamp parseTimestamp(String datetime) {
        if (datetime == null || datetime.isEmpty())
            return null;

        String normalized = datetime.replace(" ", "T");
        if (!normalized.endsWith("Z")) {
            normalized += "Z";
        }
        return Timestamp.parseTimestamp(normalized);
    }

    private String formatTimestamp(Timestamp ts) {
        if (ts == null)
            return null;
        return ts.toString().replace("T", " ").replace("Z", "");
    }

    // ====================================================================
    // CREATE
    // ====================================================================
    public DiscountResponseDto createDiscount(DiscountRequestDto dto, String createBy)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        // ====== Kiểm tra trùng ID ======
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(dto.getDiscountId());
        if (docRef.get().get().exists()) {
            throw new RuntimeException("Mã giảm giá đã tồn tại");
        }

        // ====== Validate giá trị ======
        if (dto.getValueDiscount() == null || dto.getValueDiscount() <= 0) {
            throw new RuntimeException("Giá trị giảm phải lớn hơn 0");
        }

        if ("percentage".equalsIgnoreCase(dto.getValueType()) && dto.getValueDiscount() >= 100) {
            throw new RuntimeException("Giá trị giảm phần trăm phải nhỏ hơn 100");
        }

        if (dto.getMinPriceRequired() != null && dto.getMinPriceRequired() <= 0) {
            throw new RuntimeException("Đơn tối thiểu phải lớn hơn 0");
        }

        if (dto.getMaxPriceDiscount() != null && dto.getMaxPriceDiscount() <= 0) {
            throw new RuntimeException("Giảm tối đa phải lớn hơn 0");
        }

        if (dto.getMaxQuantity() <= 0) {
            throw new RuntimeException("Số lượng tối đa phải lớn hơn 0");
        }

        // ====== Validate ngày ======
        Timestamp now = Timestamp.now();
        Timestamp start = parseTimestamp(dto.getStartDate());
        Timestamp end = parseTimestamp(dto.getEndDate());

        if (start.compareTo(now) < 0) {
            throw new RuntimeException("Ngày bắt đầu không được nhỏ hơn hiện tại");
        }

        if (end.compareTo(start) < 0) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        // ====== Create Discount ======
        Discount discount = discountMapper.toDiscount(dto);
        discount.setStartDate(start);
        discount.setEndDate(end);
        discount.setCreatedBy(createBy);
        discount.setCreatedAt(now);

        ApiFuture<WriteResult> result = docRef.set(discount);
        result.get();

        discountEventProducer.sendMessage(discount);

        // ====== Build Response ======
        DiscountResponseDto res = discountMapper.toDiscountResponseDto(discount);
        res.setStartDate(formatTimestamp(start));
        res.setEndDate(formatTimestamp(end));

        return res;
    }

    // ====================================================================
    // READ
    // ====================================================================
    public DiscountResponseDto getDiscountById(String discountId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot document = db.collection(COLLECTION_NAME)
                .document(discountId)
                .get().get();

        if (!document.exists()) {
            return null;
        }

        Discount discount = document.toObject(Discount.class);
        DiscountResponseDto res = discountMapper.toDiscountResponseDto(discount);

        res.setStartDate(formatTimestamp(discount.getStartDate()));
        res.setEndDate(formatTimestamp(discount.getEndDate()));

        return res;
    }

    // ====================================================================
    // READ ALL
    // ====================================================================
    public List<DiscountResponseDto> getAllDiscounts()
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> documents = db.collection(COLLECTION_NAME)
                .get().get().getDocuments();

        List<DiscountResponseDto> list = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            Discount discount = doc.toObject(Discount.class);
            DiscountResponseDto res = discountMapper.toDiscountResponseDto(discount);
            res.setStartDate(formatTimestamp(discount.getStartDate()));
            res.setEndDate(formatTimestamp(discount.getEndDate()));

            list.add(res);
        }
        return list;
    }

    public List<Discount> getAllDiscountsRaw()
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> documents = db.collection(COLLECTION_NAME)
                .get().get().getDocuments();

        List<Discount> discountList = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            Discount discount = document.toObject(Discount.class);
            discountList.add(discount);
        }
        return discountList;
    }

    // ====================================================================
    // UPDATE
    // ====================================================================
    public DiscountResponseDto updateDiscount(String userId, DiscountRequestDto dto)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(dto.getDiscountId());

        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Không tìm thấy mã giảm giá");
        }

        Discount existing = snapshot.toObject(Discount.class);
        if (existing == null) {
            throw new RuntimeException("Không tìm thấy mã giảm giá");
        }

        // ====== Validate giá trị ======
        if (dto.getValueDiscount() == null || dto.getValueDiscount() <= 0) {
            throw new RuntimeException("Giá trị giảm phải lớn hơn 0");
        }

        if ("percentage".equalsIgnoreCase(dto.getValueType()) && dto.getValueDiscount() >= 100) {
            throw new RuntimeException("Giá trị giảm phần trăm phải nhỏ hơn 100");
        }

        if (dto.getMinPriceRequired() != null && dto.getMinPriceRequired() <= 0) {
            throw new RuntimeException("Đơn tối thiểu phải lớn hơn 0");
        }

        if (dto.getMaxPriceDiscount() != null && dto.getMaxPriceDiscount() <= 0) {
            throw new RuntimeException("Giảm tối đa phải lớn hơn 0");
        }

        if (dto.getMaxQuantity() <= 0) {
            throw new RuntimeException("Số lượng tối đa phải lớn hơn 0");
        }

        // ====== Validate ngày khi update ======
        Timestamp start = parseTimestamp(dto.getStartDate());
        Timestamp end = parseTimestamp(dto.getEndDate());

        if (end.compareTo(start) < 0) {
            throw new RuntimeException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        // ====== Build updated Discount ======
        Discount discount = discountMapper.toDiscount(dto);

        discount.setStartDate(start);
        discount.setEndDate(end);

        discount.setCreatedAt(existing.getCreatedAt());
        discount.setCreatedBy(existing.getCreatedBy());
        discount.setUpdatedAt(Timestamp.now());
        discount.setUpdatedBy(userId);

        docRef.set(discount).get();
        discountEventProducer.sendMessage(discount);

        DiscountResponseDto res = discountMapper.toDiscountResponseDto(discount);
        res.setStartDate(formatTimestamp(start));
        res.setEndDate(formatTimestamp(end));

        return res;
    }

    // ====================================================================
    // DELETE
    // ====================================================================
    public String deleteDiscount(String discountId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(discountId);

        ApiFuture<WriteResult> result = docRef.delete();
        result.get();

        discountEventProducer.sendMessage(discountId);

        return "Discount " + discountId + " is deleted";
    }

    // ====================================================================
    // UPDATE USAGE
    // ====================================================================
    public void updateDiscountUsage(Discount discount)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("discounts").document(discount.getDiscountId());

        docRef.update("usedQuantity", discount.getUsedQuantity()).get();

        discountEventProducer.sendMessage(discount);
    }
}
