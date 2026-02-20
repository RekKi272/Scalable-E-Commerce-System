package com.hmkeyewear.product_service.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.product_service.dto.VariantImportRequestDto;
import com.hmkeyewear.product_service.dto.VariantListResponseDto;
import com.hmkeyewear.product_service.model.Product;
import com.hmkeyewear.product_service.model.Variant;

@Service
public class VariantService {

    private static final String COLLECTION = "products";
    private static final String COUNTER_COLLECTION = "counters";
    private static final String VARIANT_COUNTER_DOC = "variantCounter";

    private String generateVariantId(Firestore db, String productId)
            throws ExecutionException, InterruptedException {

        DocumentReference counterRef = db.collection(COUNTER_COLLECTION)
                .document(VARIANT_COUNTER_DOC);

        return db.runTransaction(tx -> {
            DocumentSnapshot snap = tx.get(counterRef).get();
            Long last = snap.contains(productId) ? snap.getLong(productId) : 0L;
            long next = last + 1;
            tx.set(counterRef, Map.of(productId, next), SetOptions.merge());
            return productId + String.format("%02d", next);
        }).get();
    }

    public Variant addVariant(String productId, Variant variant)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(COLLECTION).document(productId);

        db.runTransaction(tx -> {
            Product product = tx.get(ref).get().toObject(Product.class);
            if (product == null)
                throw new RuntimeException("Không tìm thấy sản phẩm");

            variant.setVariantId(generateVariantId(db, productId));

            if (variant.getQuantityImport() == null) {
                variant.setQuantityImport(0L);
            }

            variant.setQuantitySell(0L);

            product.getVariants().add(variant);
            tx.set(ref, product);
            return null;
        }).get();

        return variant;
    }

    public List<Variant> getAllVariantsOfProduct(String productId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        Product product = db.collection(COLLECTION)
                .document(productId)
                .get()
                .get()
                .toObject(Product.class);

        return product != null ? product.getVariants() : List.of();
    }

    public Variant updateVariant(String productId, Variant updated)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(COLLECTION).document(productId);

        db.runTransaction(tx -> {
            Product product = tx.get(ref).get().toObject(Product.class);

            Variant old = product.getVariants().stream()
                    .filter(v -> v.getVariantId().equals(updated.getVariantId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));

            updated.setQuantityImport(old.getQuantityImport());
            updated.setQuantitySell(old.getQuantitySell());

            product.getVariants().remove(old);
            product.getVariants().add(updated);

            tx.set(ref, product);
            return null;
        }).get();

        return updated;
    }

    public void deleteVariant(String productId, String variantId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(COLLECTION).document(productId);

        db.runTransaction(tx -> {
            Product product = tx.get(ref).get().toObject(Product.class);

            Variant v = product.getVariants().stream()
                    .filter(x -> x.getVariantId().equals(variantId))
                    .findFirst()
                    .orElseThrow();

            if (v.getQuantitySell() > 0) {
                throw new RuntimeException("Variant đã được bán");
            }

            product.getVariants().remove(v);
            tx.set(ref, product);
            return null;
        }).get();
    }

    public List<VariantListResponseDto> getAllVariants()
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        QuerySnapshot snapshot = db.collection(COLLECTION).get().get();

        List<VariantListResponseDto> result = new ArrayList<>();

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Product product = doc.toObject(Product.class);
            if (product == null || product.getVariants() == null) {
                continue;
            }

            for (Variant variant : product.getVariants()) {

                Long importQty = variant.getQuantityImport() != null
                        ? variant.getQuantityImport()
                        : 0L;
                Long sellQty = variant.getQuantitySell() != null
                        ? variant.getQuantitySell()
                        : 0L;

                VariantListResponseDto dto = new VariantListResponseDto();
                dto.setProductId(product.getProductId());
                dto.setVariantId(variant.getVariantId());
                dto.setProductName(product.getProductName());
                dto.setSellingPrice(product.getSellingPrice());
                dto.setColor(variant.getColor());
                dto.setThumbnail(variant.getThumbnail());
                dto.setStockQuantity(importQty - sellQty);

                result.add(dto);
            }
        }

        return result;
    }

    // Nhập kho
    public void importVariants(List<VariantImportRequestDto> items)
            throws ExecutionException, InterruptedException {

        for (VariantImportRequestDto item : items) {
            importVariantQuantity(item.getVariantId(), item.getQuantity());
        }
    }

    public Variant importVariantQuantity(String variantId, Long quantity)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        QuerySnapshot snapshot = db.collection(COLLECTION).get().get();

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Product product = doc.toObject(Product.class);
            if (product == null || product.getVariants() == null) {
                continue;
            }

            for (Variant variant : product.getVariants()) {
                if (variantId.equals(variant.getVariantId())) {

                    Long currentImport = variant.getQuantityImport() != null
                            ? variant.getQuantityImport()
                            : 0L;

                    variant.setQuantityImport(currentImport + quantity);

                    db.collection(COLLECTION)
                            .document(product.getProductId())
                            .set(product, SetOptions.merge());

                    return variant;
                }
            }
        }

        throw new RuntimeException("Không tìm thấy biến thể: " + variantId);
    }

}
