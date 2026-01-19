package com.hmkeyewear.product_service.service;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.product_service.mapper.ProductSearchMapper;
import com.hmkeyewear.product_service.model.Product;
import com.hmkeyewear.product_service.model.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchSyncService {

    private final ProductSearchService productSearchService;
    private final ProductSearchMapper productSearchMapper;

    private static final String COLLECTION = "products";

    private String removeVietnameseDiacritics(String str) {
        if (str == null)
            return null;
        String temp = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        temp = temp.replaceAll("\\p{M}", ""); // loại bỏ dấu
        return temp.replaceAll("đ", "d").replaceAll("Đ", "D");
    }

    public void syncAll() {

        try {
            Firestore db = FirestoreClient.getFirestore();

            List<QueryDocumentSnapshot> docs =
                    db.collection(COLLECTION).get().get().getDocuments();

            List<ProductDocument> documents = new ArrayList<>();

            for (QueryDocumentSnapshot doc : docs) {
                Product product = doc.toObject(Product.class);
                product.setProductName(removeVietnameseDiacritics(product.getProductName()));
                documents.add(productSearchMapper.toDocument(product));
            }

            documents.forEach(productSearchService::save);

            log.info("Synced {} products to Elasticsearch", documents.size());

        } catch (Exception e) {
            log.warn("Sync skipped — Firestore or ES not ready");
        }
    }
}
