package com.hmkeyewear.product_service.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.common_dto.dto.PageResponseDto;
import com.hmkeyewear.product_service.dto.AttributeRequestDto;
import com.hmkeyewear.product_service.dto.AttributeResponseDto;
import com.hmkeyewear.product_service.mapper.AttributeMapper;
import com.hmkeyewear.product_service.model.Attribute;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class AttributeService {

    private static final String COLLECTION = "attributes";
    private static final String COUNTER_COLLECTION = "counters";
    private static final String COUNTER_DOC = "attributeCounter";

    private final AttributeMapper mapper;

    public AttributeService(AttributeMapper mapper) {
        this.mapper = mapper;
    }

    private String generateAttributeId(Firestore db)
            throws ExecutionException, InterruptedException {

        DocumentReference ref = db.collection(COUNTER_COLLECTION).document(COUNTER_DOC);

        return db.runTransaction(tx -> {
            DocumentSnapshot snap = tx.get(ref).get();
            long last = snap.exists() && snap.contains("lastId") ? snap.getLong("lastId") : 0;
            long next = last + 1;
            tx.set(ref, Map.of("lastId", next), SetOptions.merge());
            return "ATTR" + String.format("%03d", next);
        }).get();
    }

    public AttributeResponseDto create(AttributeRequestDto dto, String createdBy)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        Attribute entity = mapper.toEntity(dto);
        entity.setAttributeId(generateAttributeId(db));
        entity.setCreatedAt(Timestamp.now());
        entity.setCreatedBy(createdBy);

        db.collection(COLLECTION).document(entity.getAttributeId()).set(entity);

        return mapper.toResponseDto(entity);
    }

    public AttributeResponseDto update(String attributeId, AttributeRequestDto dto)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(COLLECTION).document(attributeId);

        DocumentSnapshot snap = ref.get().get();
        if (!snap.exists()) {
            throw new RuntimeException("Attribute not found");
        }

        Attribute existing = snap.toObject(Attribute.class);
        Attribute updated = mapper.toEntity(dto);

        updated.setAttributeId(attributeId);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());

        ref.set(updated);
        return mapper.toResponseDto(updated);
    }

    public String delete(String attributeId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(COLLECTION).document(attributeId);

        DocumentSnapshot snap = ref.get().get();
        if (!snap.exists()) {
            throw new RuntimeException("Attribute not found");
        }

        Attribute attribute = snap.toObject(Attribute.class);
        if (attribute.getCategoryIds() != null && !attribute.getCategoryIds().isEmpty()) {
            throw new RuntimeException("Không thể xoá attribute đang liên kết category");
        }

        ref.delete().get();
        return "Deleted attribute " + attributeId;
    }

    public List<AttributeResponseDto> getOptions()
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        List<AttributeResponseDto> result = new ArrayList<>();

        for (QueryDocumentSnapshot doc : db.collection(COLLECTION).get().get()) {
            result.add(mapper.toResponseDto(doc.toObject(Attribute.class)));
        }
        return result;
    }

    public PageResponseDto<AttributeResponseDto> getPaging(int page, int size)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        Query base = db.collection(COLLECTION).orderBy("createdAt", Query.Direction.DESCENDING);
        long total = base.get().get().size();
        int totalPages = (int) Math.ceil((double) total / size);

        Query pageQuery = base.limit(size);
        if (page > 0) {
            QuerySnapshot prev = base.limit(page * size).get().get();
            if (!prev.isEmpty()) {
                pageQuery = pageQuery.startAfter(prev.getDocuments().get(prev.size() - 1));
            }
        }

        List<AttributeResponseDto> items = new ArrayList<>();
        for (QueryDocumentSnapshot doc : pageQuery.get().get()) {
            items.add(mapper.toResponseDto(doc.toObject(Attribute.class)));
        }

        return new PageResponseDto<>(items, page, size, total, totalPages);
    }

    public List<AttributeResponseDto> getByCategoryId(String categoryId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        Query query = db.collection(COLLECTION)
                .whereArrayContains("categoryIds", categoryId);

        List<AttributeResponseDto> result = new ArrayList<>();

        for (QueryDocumentSnapshot doc : query.get().get().getDocuments()) {
            Attribute attribute = doc.toObject(Attribute.class);
            result.add(mapper.toResponseDto(attribute));
        }

        return result;
    }

    public AttributeResponseDto updateCategoryLink(
            String attributeId,
            String categoryId,
            boolean attach)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(COLLECTION).document(attributeId);

        DocumentSnapshot snap = ref.get().get();
        if (!snap.exists()) {
            throw new RuntimeException("Attribute không tìm thấy");
        }

        Attribute attribute = snap.toObject(Attribute.class);
        List<String> categoryIds = Optional.ofNullable(attribute.getCategoryIds())
                .orElse(new ArrayList<>());

        if (attach) {
            if (categoryIds.contains(categoryId)) {
                return mapper.toResponseDto(attribute);
            }
            categoryIds.add(categoryId);
        } else {
            if (!categoryIds.remove(categoryId)) {
                return mapper.toResponseDto(attribute);
            }
        }

        ref.set(
                Map.of("categoryIds", categoryIds),
                SetOptions.merge());

        attribute.setCategoryIds(categoryIds);
        return mapper.toResponseDto(attribute);
    }

}
