package com.hmkeyewear.store_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.store_service.dto.StoreRequestDto;
import com.hmkeyewear.store_service.dto.StoreResponseDto;
import com.hmkeyewear.store_service.mapper.StoreMapper;
import com.hmkeyewear.store_service.model.Store;
import org.springframework.stereotype.Service;
import com.google.cloud.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class StoreService {

    private final StoreMapper storeMapper;

    public StoreService(StoreMapper storeMapper) {
        this.storeMapper = storeMapper;
    }

    private static final String COLLECTION_NAME = "stores";
    private static final String COUNTER_COLLECTION = "counters";
    private static final String STORE_COUNTER_DOC = "storeCounter";

    // Auto-generate storeId
    private String generateStoreId(Firestore db) throws ExecutionException, InterruptedException {
        DocumentReference counterRef = db.collection(COUNTER_COLLECTION).document(STORE_COUNTER_DOC);

        ApiFuture<String> future = db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef).get();
            long lastId = snapshot.exists() && snapshot.contains("lastId") ? snapshot.getLong("lastId") : 0;
            long newId = lastId + 1;
            transaction.set(counterRef, Map.of("lastId", newId), SetOptions.merge());
            return String.format("STORE%04d", newId);
        });

        return future.get();
    }

    // CREATE store
    public StoreResponseDto createStore(StoreRequestDto dto, String createdBy)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        String newStoreId = generateStoreId(db);

        Store store = storeMapper.toStore(dto);
        store.setStoreId(newStoreId);
        store.setCreatedAt(Timestamp.now());
        store.setCreatedBy(createdBy);

        db.collection(COLLECTION_NAME).document(newStoreId).set(store).get();
        return storeMapper.toStoreResponseDto(store);
    }

    // GET ONE
    public StoreResponseDto getStoreById(String storeId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(storeId).get().get();
        if (snapshot.exists()) {
            Store store = snapshot.toObject(Store.class);
            return storeMapper.toStoreResponseDto(store);
        }
        return null;
    }

    // GET ALL
    public List<StoreResponseDto> getAllStores() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> docs = db.collection(COLLECTION_NAME).get().get().getDocuments();

        List<StoreResponseDto> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            list.add(storeMapper.toStoreResponseDto(doc.toObject(Store.class)));
        }
        return list;
    }

    // DELETE
    public String deleteStore(String storeId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(storeId).delete().get();
        return "Deleted store " + storeId;
    }
}
