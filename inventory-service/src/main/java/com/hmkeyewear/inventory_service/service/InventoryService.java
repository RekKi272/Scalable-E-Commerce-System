package com.hmkeyewear.inventory_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.inventory_service.dto.InventoryRequestDto;
import com.hmkeyewear.inventory_service.dto.InventoryResponseDto;
import com.hmkeyewear.inventory_service.mapper.InventoryMapper;
import com.hmkeyewear.inventory_service.model.Inventory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class InventoryService {

    private final InventoryMapper inventoryMapper;
    private static final String COLLECTION_NAME = "inventory";

    public InventoryService(InventoryMapper inventoryMapper) {
        this.inventoryMapper = inventoryMapper;
    }

    /**
     * Cập nhật 1 sản phẩm, cộng dồn số lượng và kiểm tra không bán âm.
     * type: "IMPORT" hoặc "SELL" (truyền từ controller)
     */
    public InventoryResponseDto updateInventoryWithType(InventoryRequestDto dto, String storeId, String username, String type)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        String docId = storeId + "_" + dto.getProductId() + "_" + dto.getVariantId();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(docId);

        Inventory updatedInventory = db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(docRef).get();
            Inventory inv;

            if (snapshot.exists()) {
                inv = snapshot.toObject(Inventory.class);
            } else {
                inv = new Inventory();
                inv.setInventoryId(storeId);
                inv.setProductId(dto.getProductId());
                inv.setVariantId(dto.getVariantId());
                inv.setQuantityImport(0L);
                inv.setQuantitySell(0L);
                inv.setCreatedAt(com.google.cloud.Timestamp.now());
                inv.setCreatedBy(username);
            }

            if ("IMPORT".equalsIgnoreCase(type)) {
                inv.setQuantityImport(inv.getQuantityImport() + dto.getQuantity());
            } else if ("SELL".equalsIgnoreCase(type)) {
                long newSell = inv.getQuantitySell() + dto.getQuantity();
                if (inv.getQuantityImport() - newSell < 0) {
                    throw new IllegalArgumentException(
                        "Không đủ hàng để bán. Số lượng hiện tại: " +
                        (inv.getQuantityImport() - inv.getQuantitySell())
                    );
                }
                inv.setQuantitySell(newSell);
            } else {
                throw new IllegalArgumentException("Type must be IMPORT or SELL");
            }

            inv.setUpdatedAt(com.google.cloud.Timestamp.now());
            inv.setUpdatedBy(username);

            transaction.set(docRef, inv);
            return inv;
        }).get();

        return inventoryMapper.toInventoryResponseDto(updatedInventory);
    }

    /**
     * Cập nhật nhiều sản phẩm cùng lúc (batch), cộng dồn số lượng và kiểm tra không bán âm.
     * type: "IMPORT" hoặc "SELL" (truyền từ controller)
     */
    public List<InventoryResponseDto> updateInventoryBatchWithType(List<InventoryRequestDto> dtos, String storeId, String username, String type)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        WriteBatch batch = db.batch();
        List<InventoryResponseDto> responses = new ArrayList<>();

        Map<String, Inventory> existingInventoryMap = new HashMap<>();
        for (InventoryRequestDto dto : dtos) {
            String docId = storeId + "_" + dto.getProductId() + "_" + dto.getVariantId();
            DocumentSnapshot snap = db.collection(COLLECTION_NAME).document(docId).get().get();
            if (snap.exists()) {
                existingInventoryMap.put(docId, snap.toObject(Inventory.class));
            }
        }

        for (InventoryRequestDto dto : dtos) {
            String docId = storeId + "_" + dto.getProductId() + "_" + dto.getVariantId();
            Inventory inv = existingInventoryMap.getOrDefault(docId, new Inventory());

            if (!existingInventoryMap.containsKey(docId)) {
                inv.setInventoryId(storeId);
                inv.setProductId(dto.getProductId());
                inv.setVariantId(dto.getVariantId());
                inv.setQuantityImport(0L);
                inv.setQuantitySell(0L);
                inv.setCreatedAt(com.google.cloud.Timestamp.now());
                inv.setCreatedBy(username);
            }

            if ("IMPORT".equalsIgnoreCase(type)) {
                inv.setQuantityImport(inv.getQuantityImport() + dto.getQuantity());
            } else if ("SELL".equalsIgnoreCase(type)) {
                long newSell = inv.getQuantitySell() + dto.getQuantity();
                if (inv.getQuantityImport() - newSell < 0) {
                    throw new IllegalArgumentException(
                        "Không đủ hàng để bán sản phẩm " + dto.getProductId() +
                        " biến thể " + dto.getVariantId() + ". Số lượng hiện tại: " +
                        (inv.getQuantityImport() - inv.getQuantitySell())
                    );
                }
                inv.setQuantitySell(newSell);
            } else {
                throw new IllegalArgumentException("Type must be IMPORT or SELL");
            }

            inv.setUpdatedAt(com.google.cloud.Timestamp.now());
            inv.setUpdatedBy(username);

            batch.set(db.collection(COLLECTION_NAME).document(docId), inv);
            responses.add(inventoryMapper.toInventoryResponseDto(inv));
        }

        batch.commit().get();
        return responses;
    }


    /**
     * Lấy tất cả inventory của 1 cửa hàng
     */
    public List<InventoryResponseDto> getByStore(String storeId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference collection = db.collection(COLLECTION_NAME);
        Query query = collection.whereEqualTo("inventoryId", storeId);
        List<QueryDocumentSnapshot> documents = query.get().get().getDocuments();

        List<InventoryResponseDto> result = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            Inventory inv = doc.toObject(Inventory.class);
            result.add(inventoryMapper.toInventoryResponseDto(inv));
        }
        return result;
    }

    /**
     * Lấy tất cả inventory của tất cả cửa hàng
     */
    public List<InventoryResponseDto> getAll() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        CollectionReference collection = db.collection(COLLECTION_NAME);
        List<QueryDocumentSnapshot> documents = collection.get().get().getDocuments();

        List<InventoryResponseDto> result = new ArrayList<>();
        for (DocumentSnapshot doc : documents) {
            Inventory inv = doc.toObject(Inventory.class);
            result.add(inventoryMapper.toInventoryResponseDto(inv));
        }
        return result;
    }
}
