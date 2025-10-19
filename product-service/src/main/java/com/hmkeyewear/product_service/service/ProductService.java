package com.hmkeyewear.product_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.product_service.dto.ProductRequestDto;
import com.hmkeyewear.product_service.dto.ProductResponseDto;
import com.hmkeyewear.product_service.feign.ProductInterface;
import com.hmkeyewear.product_service.mapper.ProductMapper;
import com.hmkeyewear.product_service.model.Customer;
import com.hmkeyewear.product_service.model.Product;
import com.hmkeyewear.product_service.model.Variant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class ProductService {

    @Autowired
    private ProductInterface productInterface;

    @Autowired
    private ProductMapper productMapper;

    private static final String COLLECTION_NAME = "products";
    private static final String COUNTER_COLLECTION = "counters";
    private static final String PRODUCT_COUNTER_DOC = "productCounter";
    private static final String VARIANT_COUNTER_DOC = "variantCounter";

    // Lấy tên khách hàng từ user-service
    public String getCustomer(String customerId) throws ExecutionException, InterruptedException {
        Customer customer = productInterface.getCustomer(customerId);
        return customer.getFirstName();
    }

    private String generateProductId(Firestore db) throws ExecutionException, InterruptedException {
        DocumentReference counterRef = db.collection(COUNTER_COLLECTION).document(PRODUCT_COUNTER_DOC);

        ApiFuture<String> future = db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef).get();

            Long lastIdObj = (snapshot.exists() && snapshot.contains("lastId")) ? snapshot.getLong("lastId") : null;
            long lastId = (lastIdObj != null) ? lastIdObj : 0;

            long newId = lastId + 1;
            transaction.set(counterRef, Map.of("lastId", newId), SetOptions.merge());

            // --- Fix lỗi dữ chuỗi ---
            String formattedId = String.format("%03d", newId);
            if (formattedId.length() > 3)
                formattedId = String.valueOf(newId); // giữ nguyên nếu vượt 999
            return "PROD" + formattedId;
        });

        return future.get();
    }

    // Tạo VariantId theo productId
    private String generateVariantId(Firestore db, String productId) throws ExecutionException, InterruptedException {
        DocumentReference counterRef = db.collection(COUNTER_COLLECTION).document(VARIANT_COUNTER_DOC);

        ApiFuture<String> future = db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef).get();

            Long lastIdObj = (snapshot.exists() && snapshot.contains(productId)) ? snapshot.getLong(productId) : null;
            long lastId = (lastIdObj != null) ? lastIdObj : 0;

            long newId = lastId + 1;

            // Dùng set với merge để tạo field mới nếu chưa tồn tại
            transaction.set(counterRef, Map.of(productId, newId), SetOptions.merge());

            // --- Fix lỗi dữ chuỗi ---
            String formattedVariant = String.format("%02d", newId);
            if (formattedVariant.length() > 2)
                formattedVariant = String.valueOf(newId); // giữ nguyên nếu vượt 99
            return productId + formattedVariant; // PROD00101
        });

        return future.get();
    }

    // CREATE product
    public ProductResponseDto createProduct(ProductRequestDto dto, String userId)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        String newProductId = generateProductId(db);

        Product product = new Product();
        product.setProductId(newProductId);
        product.setCategoryId(dto.getCategoryId());
        product.setBrandId(dto.getBrandId());
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setStatus(dto.getStatus());
        product.setThumbnail(dto.getThumbnail());
        product.setImportPrice(dto.getImportPrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setAttributes(dto.getAttributes());
        product.setCreatedAt(Timestamp.now());
        product.setCreatedBy(userId); // <-- từ header
        product.setUpdatedAt(Timestamp.now());
        product.setUpdatedBy(userId); // <-- từ header

        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            List<Variant> variantsWithId = new ArrayList<>();
            for (Variant v : dto.getVariants()) {
                v.setVariantId(generateVariantId(db, newProductId));
                variantsWithId.add(v);
            }
            product.setVariants(variantsWithId);
        }

        if (dto.getImages() != null) {
            product.setImages(dto.getImages());
        }

        db.collection(COLLECTION_NAME).document(product.getProductId()).set(product).get();

        return productMapper.toProductResponseDto(product);
    }

    // GET Product by Id
    public ProductResponseDto getProductById(String productId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(productId).get().get();

        if (snapshot.exists()) {
            Product product = snapshot.toObject(Product.class);
            if (product == null) {
                throw new RuntimeException("Product data is null for ID " + productId);
            }
            return productMapper.toProductResponseDto(product);
        }
        return null;
    }

    // GET All Products
    public List<ProductResponseDto> getAllProducts() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> documents = db.collection(COLLECTION_NAME).get().get().getDocuments();

        List<ProductResponseDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Product product = doc.toObject(Product.class);
            if (product != null) {
                result.add(productMapper.toProductResponseDto(product));
            }
        }
        return result;
    }

    // UPDATE Product
    public ProductResponseDto updateProduct(String productId, ProductRequestDto dto, String userId)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference productRef = db.collection(COLLECTION_NAME).document(productId);

        DocumentSnapshot snapshot = productRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Product with ID " + productId + " not found");
        }

        Product existingProduct = snapshot.toObject(Product.class);
        if (existingProduct == null) {
            throw new RuntimeException("Product data is null for ID " + productId);
        }

        Product updatedProduct = productMapper.toProduct(dto);
        updatedProduct.setProductId(productId);
        updatedProduct.setCreatedAt(existingProduct.getCreatedAt());
        updatedProduct.setCreatedBy(existingProduct.getCreatedBy());
        updatedProduct.setUpdatedAt(Timestamp.now());
        updatedProduct.setUpdatedBy(userId); // <-- từ header

        if (dto.getVariants() != null && !dto.getVariants().isEmpty()) {
            List<Variant> variantsWithId = new ArrayList<>();
            for (Variant v : dto.getVariants()) {
                if (v.getVariantId() == null || v.getVariantId().isEmpty()) {
                    v.setVariantId(generateVariantId(db, productId));
                }
                variantsWithId.add(v);
            }
            updatedProduct.setVariants(variantsWithId);
        }

        productRef.set(updatedProduct).get();

        return productMapper.toProductResponseDto(updatedProduct);
    }

    // DELETE Product
    public String deleteProduct(String productId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(productId).delete().get();
        return "Successfully deleted product with id " + productId;
    }
}
