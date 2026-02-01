package com.hmkeyewear.cart_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.common_dto.dto.PageResponseDto;
import com.hmkeyewear.cart_service.dto.DiscountRequestDto;
import com.hmkeyewear.cart_service.dto.DiscountResponseDto;
import com.hmkeyewear.cart_service.mapper.DiscountMapper;
import com.hmkeyewear.cart_service.messaging.DiscountEventProducer;
import com.hmkeyewear.cart_service.model.Discount;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.time.LocalDate;
import java.time.ZoneId;

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
    public PageResponseDto<DiscountResponseDto> getDiscountsPaging(int page, int size)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        Query baseQuery = db.collection(COLLECTION_NAME)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        long totalElements = baseQuery.get().get().size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        Query pageQuery = baseQuery.limit(size);

        if (page > 0) {
            QuerySnapshot prev = baseQuery
                    .limit((int) ((long) page * size))
                    .get()
                    .get();

            if (!prev.isEmpty()) {
                DocumentSnapshot lastDoc = prev.getDocuments().get(prev.size() - 1);
                pageQuery = pageQuery.startAfter(lastDoc);
            }
        }

        List<DiscountResponseDto> items = new ArrayList<>();
        for (QueryDocumentSnapshot doc : pageQuery.get().get().getDocuments()) {
            Discount discount = doc.toObject(Discount.class);
            DiscountResponseDto res = discountMapper.toDiscountResponseDto(discount);
            res.setStartDate(formatTimestamp(discount.getStartDate()));
            res.setEndDate(formatTimestamp(discount.getEndDate()));
            items.add(res);
        }

        return new PageResponseDto<>(
                items,
                page,
                size,
                totalElements,
                totalPages);
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

        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Không tìm thấy mã giảm giá");
        }

        Discount discount = snapshot.toObject(Discount.class);
        if (discount == null) {
            throw new RuntimeException("Không tìm thấy mã giảm giá");
        }

        // ====== KIỂM TRA ĐÃ CÓ NGƯỜI DÙNG CHƯA ======
        if (discount.getUsedQuantity() > 0) {
            throw new RuntimeException("Không thể xóa mã giảm giá vì đã có đơn hàng sử dụng");
        }

        // ====== CHƯA AI DÙNG → ĐƯỢC XÓA ======
        docRef.delete().get();
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

    public Discount validateDiscountUsable(String discountId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot snapshot = db.collection(COLLECTION_NAME)
                .document(discountId)
                .get().get();

        if (!snapshot.exists()) {
            throw new RuntimeException("Mã giảm giá không tồn tại");
        }

        Discount discount = snapshot.toObject(Discount.class);
        if (discount == null) {
            throw new RuntimeException("Mã giảm giá không hợp lệ");
        }

        // ===== CHỈ SO SÁNH NGÀY =====
        LocalDate today = LocalDate.now();

        LocalDate startDate = discount.getStartDate()
                .toDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate endDate = discount.getEndDate()
                .toDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (today.isBefore(startDate) || today.isAfter(endDate)) {
            throw new RuntimeException("Mã giảm giá đã hết hạn hoặc chưa đến thời gian sử dụng");
        }

        // ===== KIỂM TRA SỐ LƯỢNG =====
        if (discount.getUsedQuantity() >= discount.getMaxQuantity()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
        }

        return discount;
    }
}
