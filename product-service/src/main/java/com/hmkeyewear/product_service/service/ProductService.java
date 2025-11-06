package com.hmkeyewear.product_service.service;

import com.algolia.api.SearchClient;
import com.algolia.model.search.*;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.product_service.dto.ProductInforResponseDto;
import com.hmkeyewear.product_service.dto.ProductRequestDto;
import com.hmkeyewear.product_service.dto.ProductResponseDto;
import com.hmkeyewear.product_service.feign.ProductInterface;
import com.hmkeyewear.product_service.mapper.ProductMapper;
import com.hmkeyewear.product_service.model.Customer;
import com.hmkeyewear.product_service.model.Product;
import com.hmkeyewear.product_service.model.ProductLite;
import com.hmkeyewear.product_service.model.Variant;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductInterface productInterface;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SearchClient searchClient;



    private static final String COLLECTION_NAME = "products";
    private static final String COUNTER_COLLECTION = "counters";
    private static final String PRODUCT_COUNTER_DOC = "productCounter";
    private static final String VARIANT_COUNTER_DOC = "variantCounter";
    private static final String ALGOLIA_INDEX_NAME = "products";

    // Lấy tên khách hàng từ user-service
    public String getCustomer(String customerId) throws ExecutionException, InterruptedException {
        Customer customer = productInterface.getCustomer(customerId);
        return customer.getFirstName();
    }

    // ------------------- UTIL: Remove Vietnamese Diacritics -------------------
    private String removeVietnameseDiacritics(String str) {
        if (str == null) return null;
        String temp = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        temp = temp.replaceAll("\\p{M}", ""); // loại bỏ dấu
        return temp.replaceAll("đ", "d").replaceAll("Đ", "D");
    }


    // ----------------------------- SYNC EXISTING DATA ----------------------------
    @PostConstruct
    public void syncAllProductsToAlgolia() {
        try {
            Firestore db = FirestoreClient.getFirestore();

            // Lấy danh sách productId hiện có trong Algolia
            SearchResponse<ProductLite> existing = searchClient.searchSingleIndex(
                    ALGOLIA_INDEX_NAME,
                    new SearchParamsObject().setQuery(""), // truy vấn rỗng để lấy toàn bộ
                    ProductLite.class
            );

            List<String> existingIds = existing.getHits().stream()
                    .map(ProductLite::getProductId)
                    .toList();

            // Lấy toàn bộ dữ liệu từ Firestore
            List<QueryDocumentSnapshot> docs = db.collection(COLLECTION_NAME)
                    .get()
                    .get()
                    .getDocuments();

            if (docs.isEmpty()) {
                System.out.println("Không có sản phẩm nào để đồng bộ lên Algolia.");
                return;
            }

            // Tạo danh sách sản phẩm mới chưa có trong Algolia
            List<ProductLite> newProducts = new ArrayList<>();

            for (QueryDocumentSnapshot doc : docs) {
                Product product = doc.toObject(Product.class);

                String productId = product.getProductId();
                if (existingIds.contains(productId)) continue; // đã tồn tại thì bỏ qua

                ProductLite lite = new ProductLite();
                lite.setProductId(productId);
                lite.setProductName(removeVietnameseDiacritics(product.getProductName()));

                newProducts.add(lite);
            }

            // Sync lên Algolia nếu có sản phẩm mới
            if (!newProducts.isEmpty()) {
                searchClient.saveObjects(ALGOLIA_INDEX_NAME, newProducts);
                System.out.println("Đã đồng bộ " + newProducts.size() + " sản phẩm mới lên Algolia.");
            } else {
                System.out.println("Không có sản phẩm mới cần đồng bộ.");
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi đồng bộ dữ liệu lên Algolia: " + e.getMessage());
        }
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

        // --- Đồng bộ với Algolia ---
        searchClient.saveObjects(ALGOLIA_INDEX_NAME, List.of(product));

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
            result.add(productMapper.toProductResponseDto(product));
        }
        return result;
    }

    // SEARCH PRODUCT NAME BY KEYWORD
    public List<ProductInforResponseDto> searchProductByName(String keyword)
            throws ExecutionException, InterruptedException {

        String normalizedKeyword = removeVietnameseDiacritics(keyword);

        // Tìm kiếm trên Algolia (chỉ lấy productId)
        SearchResponse<ProductLite> response = searchClient.searchSingleIndex(
                ALGOLIA_INDEX_NAME,
                new SearchParamsObject()
                        .setQuery(normalizedKeyword)
                        .setRestrictSearchableAttributes(List.of("productName"))
                        .setAttributesToRetrieve(List.of("productId")), //chỉ lấy ID
                ProductLite.class
        );

        // Lấy danh sách productId từ kết quả Algolia
        List<String> productIds = response.getHits().stream()
                .map(ProductLite::getProductId)
                .toList();

        // Lấy chi tiết sản phẩm từ Firestore dựa trên ID
        Firestore firestore = FirestoreClient.getFirestore();
        List<ProductInforResponseDto> result = new ArrayList<>();

        for (String id : productIds) {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot doc = future.get();

            if (doc.exists()) {
                Product product = doc.toObject(Product.class);
                ProductInforResponseDto dto = productMapper.toProductInforResponseDto(product);
                result.add(dto);
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

        // --- Đồng bộ Algolia ---
        searchClient.saveObjects(ALGOLIA_INDEX_NAME, List.of(updatedProduct));

        return productMapper.toProductResponseDto(updatedProduct);
    }

    // GET Active Product ONLY
    public List<ProductInforResponseDto> getActiveProducts() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> queryFuture =
                db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "ACTIVE")
                .get();

        List<QueryDocumentSnapshot> documents = queryFuture.get().getDocuments();
        List<ProductInforResponseDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Product product = doc.toObject(Product.class);
            result.add(productMapper.toProductInforResponseDto(product));
        }

        return result;
    }

    // DELETE Product
    public String deleteProduct(String productId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(productId).delete().get();

        // --- Xóa khỏi Algolia ---
        searchClient.deleteObjects(ALGOLIA_INDEX_NAME, List.of(productId));

        return "Successfully deleted product with id " + productId;
    }
}
