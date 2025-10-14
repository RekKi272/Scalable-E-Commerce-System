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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ProductService {

    @Autowired
    ProductInterface productInterface;

    @Autowired
    public ProductMapper productMapper;

    public String getCustomer(String customerId) throws ExecutionException, InterruptedException {
        Customer customer = productInterface.getCustomer(customerId);
        return customer.getFirstName();
    }

    private static final String COLLECTION_NAME = "products";
    private static final String COUNTER_COLLECTION = "counters";
    private static final String PRODUCT_COUNTER_DOC = "productCounter";

    // GENERATE ProductId
    private String generateProductId(Firestore db) throws ExecutionException, InterruptedException {
        DocumentReference counterRef = db.collection(COUNTER_COLLECTION).document(PRODUCT_COUNTER_DOC);

        ApiFuture<String> future = db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef).get();

            long lastId = 0;
            if (snapshot.exists() && snapshot.contains("lastId")) {
                lastId = snapshot.getLong("lastId");
            }

            long newId = lastId + 1;
            transaction.update(counterRef, "lastId", newId);

            // format: PROD0001, PROD0002
            return String.format("PROD%04d", newId);
        });

        return future.get();
    }

    // CREATE Product
    public ProductResponseDto createProduct(ProductRequestDto dto, String createdBy)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Generate new ID
        String newProductId = generateProductId(db);

        Product product = new Product();
        product.setProductId(newProductId);
        product.setCategoryId(dto.getCategoryId());
        product.setBrandId(dto.getBrandId());

        product.setCreatedAt(Timestamp.now());

        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setStatus(dto.getStatus());
        product.setThumbnail(dto.getThumbnail());
        product.setImportPrice(dto.getImportPrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setAttributes(dto.getAttributes());
        product.setVariants(dto.getVariants());
        product.setCreatedBy(dto.getCreatedBy());
        product.setUpdatedBy(dto.getUpdatedBy());

        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(product.getProductId())
                .set(product);

        future.get();
        return productMapper.toProductResponseDto(product);
    }

    // Get Product by Id
    public ProductResponseDto getProductById(String productId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference productRef = db.collection(COLLECTION_NAME).document(productId);
        ApiFuture<DocumentSnapshot> future = productRef.get();
        DocumentSnapshot snapshot = future.get();

        if (snapshot.exists()) {
            Product product = snapshot.toObject(Product.class);
            return productMapper.toProductResponseDto(product);
        }
        return null;
    }

    // GET ALL Product
    public List<ProductResponseDto> getAllProducts() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> future = db.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<ProductResponseDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Product product = doc.toObject(Product.class);
            result.add(productMapper.toProductResponseDto(product));
        }
        return result;
    }

    // UPDATE Product
    public ProductResponseDto updateProduct(String productId, ProductRequestDto dto, String updatedBy)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference productRef = db.collection(COLLECTION_NAME).document(productId);

        // Check existed
        ApiFuture<DocumentSnapshot> future = productRef.get();
        DocumentSnapshot document = future.get();
        if (!document.exists()) {
            throw new RuntimeException("Product with ID" + productId + " not found");
        }

        // get old data
        Product existingProduct = document.toObject(Product.class);

        // Map dto to product
        Product updatedProduct = productMapper.toProduct(dto);

        updatedProduct.setProductId(productId);
        updatedProduct.setCreatedAt(existingProduct.getCreatedAt());
        updatedProduct.setCreatedBy(existingProduct.getCreatedBy());

        // Set updated info
        updatedProduct.setUpdatedAt(Timestamp.now());
        updatedProduct.setUpdatedBy(updatedBy);

        // Store to Firestore
        ApiFuture<WriteResult> writeResult = productRef.set(updatedProduct);
        writeResult.get(); // wait until done

        // return DTO
        return productMapper.toProductResponseDto(updatedProduct);
    }

    // DELETE product
    public String deleteProduct(String productId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<WriteResult> future = db.collection(COLLECTION_NAME)
                .document(productId)
                .delete();
        future.get(); // wait until delete is done
        return "Successfully deleted product with id " + productId;
    }
}
