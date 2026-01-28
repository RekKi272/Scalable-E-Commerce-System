package com.hmkeyewear.product_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import com.hmkeyewear.product_service.dto.ProductInforResponseDto;
import com.hmkeyewear.product_service.dto.ProductRequestDto;
import com.hmkeyewear.product_service.dto.ProductResponseDto;
import com.hmkeyewear.product_service.dto.ItemRequestDto;
import com.hmkeyewear.product_service.mapper.ProductMapper;
import com.hmkeyewear.product_service.mapper.ProductSearchMapper;
import com.hmkeyewear.product_service.messaging.ProductEventProducer;
import com.hmkeyewear.product_service.model.Product;
import com.hmkeyewear.product_service.model.ProductDocument;
import com.hmkeyewear.product_service.model.Variant;
import org.springframework.stereotype.Service;

import com.google.cloud.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductEventProducer productEventProducer;
    private final ProductSearchService productSearchService;
    private final ProductSearchMapper productSearchMapper;

    private static final String COLLECTION_NAME = "products";
    private static final String COUNTER_COLLECTION = "counters";
    private static final String PRODUCT_COUNTER_DOC = "productCounter";
    private static final String VARIANT_COUNTER_DOC = "variantCounter";

    // Constructor
    public ProductService(ProductMapper productMapper,
                          ProductEventProducer productEventProducer,
                          ProductSearchService productSearchService,
                          ProductSearchMapper productSearchMapper) {
        this.productMapper = productMapper;
        this.productEventProducer = productEventProducer;
        this.productSearchService = productSearchService;
        this.productSearchMapper = productSearchMapper;
    }

    // ------------------- UTIL: Remove Vietnamese Diacritics -------------------
    private String removeVietnameseDiacritics(String str) {
        if (str == null)
            return null;
        String temp = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        temp = temp.replaceAll("\\p{M}", ""); // loại bỏ dấu
        return temp.replaceAll("đ", "d").replaceAll("Đ", "D");
    }

    // ----------------------------- SYNC EXISTING DATA ----------------------------


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

        if (dto.getVariants() == null || dto.getVariants().isEmpty()) {
            throw new RuntimeException("Product must contain at least 1 variant.");
        }

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
        product.setCreatedBy(userId);
        product.setUpdatedAt(Timestamp.now());
        product.setUpdatedBy(userId);

        // --- Xử lý biến thể ---
        List<Variant> variants = new ArrayList<>();

        for (Variant v : dto.getVariants()) {
            v.setVariantId(generateVariantId(db, newProductId));
            v.setQuantityImport(0L);
            v.setQuantitySell(0L);
            variants.add(v);
        }

        product.setVariants(variants);

        if (dto.getImages() != null) {
            product.setImages(dto.getImages());
        }

        db.collection(COLLECTION_NAME).document(product.getProductId()).set(product).get();

        // index Elasticsearch
        ProductDocument productDoc = productSearchMapper.toDocument(product);
        productDoc.setProductName(removeVietnameseDiacritics(product.getProductName()));
        productSearchService.save(productDoc);

        productEventProducer.sendMessage(product);

        return productMapper.toProductResponseDto(product);
    }

    // Update Product stock
    public void updateStock(List<OrderDetailRequestDto> items) {
        Firestore db = FirestoreClient.getFirestore();

        // Gom các items theo productId
        Map<String, List<OrderDetailRequestDto>> grouped = items.stream()
                .collect(Collectors.groupingBy(OrderDetailRequestDto::getProductId));

        // Mỗi product sẽ chạy 1 transaction
        for (String productId : grouped.keySet()) {

            db.runTransaction(transaction -> {
                DocumentReference productRef = db.collection(COLLECTION_NAME).document(productId);
                DocumentSnapshot snapshot = transaction.get(productRef).get();

                if (!snapshot.exists()) {
                    throw new RuntimeException("Product not found: " + productId);
                }

                Product product = snapshot.toObject(Product.class);
                if (product == null) {
                    throw new RuntimeException("Product data null for: " + productId);
                }

                List<Variant> variants = product.getVariants();
                if (variants == null) {
                    throw new RuntimeException("Product has no variants: " + productId);
                }

                // Lấy danh sách order items thuộc product này
                List<OrderDetailRequestDto> productDetails = grouped.get(productId);

                // Cập nhật tồn kho cho từng order detail
                for (OrderDetailRequestDto detail : productDetails) {
                    Variant variant = variants.stream()
                            .filter(v -> v.getVariantId().equals(detail.getVariantId()))
                            .findFirst()
                            .orElse(null);

                    if (variant == null) {
                        throw new RuntimeException(
                                "Variant " + detail.getVariantId() + " not found in product " + productId);
                    }

                    // Kiểm tra tồn kho
                    long currentStock = (variant.getQuantityImport() != null ? variant.getQuantityImport() : 0L)
                            - (variant.getQuantitySell() != null ? variant.getQuantitySell() : 0L);

                    if (currentStock < detail.getQuantity()) {
                        throw new RuntimeException(
                                "Không đủ hàng cho variant " + variant.getVariantId() +
                                        " (còn: " + currentStock +
                                        ", yêu cầu: " + detail.getQuantity() + ")");
                    }

                    // Cập nhật quantitySell (giả sử đây là bán hàng)
                    Long newSellQty = (variant.getQuantitySell() != null ? variant.getQuantitySell() : 0L)
                            + detail.getQuantity();
                    variant.setQuantitySell(newSellQty);
                }

                // Lưu lại danh sách variants mới
                transaction.update(productRef, "variants", variants);

                return null;
            });
        }
    }

    // REFUND / CANCEL ORDER -> return stock
    public void refundStock(List<OrderDetailRequestDto> items) {
        Firestore db = FirestoreClient.getFirestore();

        // Gom item theo productId
        Map<String, List<OrderDetailRequestDto>> grouped =
                items.stream()
                        .collect(Collectors.groupingBy(OrderDetailRequestDto::getProductId));

        for (String productId : grouped.keySet()) {
            db.runTransaction(transaction -> {

                DocumentReference productRef =
                        db.collection(COLLECTION_NAME).document(productId);

                DocumentSnapshot snapshot = transaction.get(productRef).get();

                if (!snapshot.exists()) {
                    throw new RuntimeException("Product not found: " + productId);
                }

                Product product = snapshot.toObject(Product.class);
                if (product == null || product.getVariants() == null) {
                    throw new RuntimeException("Invalid product data: " + productId);
                }

                List<Variant> variants = product.getVariants();
                List<OrderDetailRequestDto> productDetails = grouped.get(productId);

                for (OrderDetailRequestDto detail : productDetails) {

                    Variant variant = variants.stream()
                            .filter(v -> v.getVariantId().equals(detail.getVariantId()))
                            .findFirst()
                            .orElse(null);

                    if (variant == null) {
                        throw new RuntimeException(
                                "Variant " + detail.getVariantId() +
                                        " not found in product " + productId
                        );
                    }

                    Long sellQty =
                            variant.getQuantitySell() != null
                                    ? variant.getQuantitySell()
                                    : 0L;

                    // không cho refund vượt quá số đã bán
                    if (sellQty < detail.getQuantity()) {
                        throw new RuntimeException(
                                "Refund quantity exceeds sold quantity for variant "
                                        + variant.getVariantId()
                                        + " (sold: " + sellQty
                                        + ", refund: " + detail.getQuantity() + ")"
                        );
                    }

                    // trả hàng -> giảm quantitySell
                    variant.setQuantitySell(sellQty - detail.getQuantity());
                }

                // cập nhật lại variants
                transaction.update(productRef, "variants", variants);

                return null;
            });
        }
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


    // Filter Product
    public List<ProductInforResponseDto> filterProducts(
            String brandId,
            String categoryId,
            Double minPrice,
            Double maxPrice)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Bắt đầu query collection "products"
        Query query = db.collection(COLLECTION_NAME);

        // Luôn lọc theo trạng thái ACTIVE
        query = query.whereEqualTo("status", "ACTIVE");

        // Lọc theo brand
        if (brandId != null && !brandId.isEmpty()) {
            query = query.whereEqualTo("brandId", brandId);
        }

        // Lọc theo category
        if (categoryId != null && !categoryId.isEmpty()) {
            query = query.whereEqualTo("categoryId", categoryId);
        }

        // Lọc theo giá bán (Firestore chỉ cho phép <= và >= trong 1 trường)
        if (minPrice != null) {
            query = query.whereGreaterThanOrEqualTo("sellingPrice", minPrice);
        }
        if (maxPrice != null) {
            query = query.whereLessThanOrEqualTo("sellingPrice", maxPrice);
        }

        // Thực thi query
        ApiFuture<QuerySnapshot> querySnapshotFuture = query.get();
        QuerySnapshot querySnapshot = querySnapshotFuture.get();

        // Chuyển thành danh sách Product
        List<Product> products = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            Product product = document.toObject(Product.class);
            if (product != null) {
                products.add(product);
            }
        }

        // Map sang DTO trả về
        return products.stream()
                .map(productMapper::toProductInforResponseDto)
                .collect(Collectors.toList());
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

        Product existing = snapshot.toObject(Product.class);
        if (existing == null) {
            throw new RuntimeException("Product data is null for ID " + productId);
        }

        // Map các field cơ bản
        Product updatedProduct = productMapper.toProduct(dto);
        updatedProduct.setProductId(productId);
        updatedProduct.setCreatedAt(existing.getCreatedAt());
        updatedProduct.setCreatedBy(existing.getCreatedBy());
        updatedProduct.setUpdatedAt(Timestamp.now());
        updatedProduct.setUpdatedBy(userId);

        // ------------------------
        // QUẢN LÝ DANH SÁCH BIẾN THỂ
        // ------------------------

        List<Variant> oldVariants = existing.getVariants() != null ? existing.getVariants() : new ArrayList<>();
        Map<String, Variant> oldMap = oldVariants.stream()
                .collect(Collectors.toMap(Variant::getVariantId, v -> v));

        List<Variant> newVariants = dto.getVariants() != null ? dto.getVariants() : new ArrayList<>();
        List<Variant> finalList = new ArrayList<>();

        // 1. DUYỆT DANH SÁCH FE GỬI LÊN
        for (Variant v : newVariants) {

            // CASE A — Biến thể CŨ (giữ nguyên ID)
            if (v.getVariantId() != null && oldMap.containsKey(v.getVariantId())) {
                Variant old = oldMap.get(v.getVariantId());

                // Giữ lại số lượng cũ
                v.setQuantityImport(old.getQuantityImport());
                v.setQuantitySell(old.getQuantitySell());

                finalList.add(v);
            }

            // CASE B — Biến thể mới
            else {
                v.setVariantId(generateVariantId(db, productId));
                v.setQuantityImport(0L);
                v.setQuantitySell(0L);

                finalList.add(v);
            }
        }

        // 2. GIỮ LẠI BIẾN THỂ CŨ KHÔNG ĐƯỢC GỬI LÊN (TRỪ KHI CHO PHÉP XOÁ)
        for (Variant old : oldVariants) {

            boolean FE_khong_gui_len = newVariants.stream()
                    .noneMatch(v -> old.getVariantId().equals(v.getVariantId()));

            if (FE_khong_gui_len) {

                long importQty = old.getQuantityImport() != null ? old.getQuantityImport() : 0;
                long sellQty = old.getQuantitySell() != null ? old.getQuantitySell() : 0;

                boolean coTheXoa = (importQty == 0 && sellQty == 0);

                if (!coTheXoa) {
                    // Không được phép xoá → giữ lại biến thể cũ
                    finalList.add(old);
                }
                // Nếu số lượng = 0 → cho phép xoá → không thêm vào finalList
            }
        }

        updatedProduct.setVariants(finalList);

        // ------------------------
        // Lưu vào DB
        // ------------------------
        productRef.set(updatedProduct).get();

        // Update in Elasticsearch
        ProductDocument productDoc = productSearchMapper.toDocument(updatedProduct);
        productDoc.setProductName(removeVietnameseDiacritics(updatedProduct.getProductName()));
        productSearchService.save(productDoc);
        productEventProducer.sendMessage(updatedProduct);

        return productMapper.toProductResponseDto(updatedProduct);
    }

    // GET Active Product ONLY
    public List<ProductInforResponseDto> getActiveProducts() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> queryFuture = db.collection(COLLECTION_NAME)
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

        // Delete from Elasticsearch
        productSearchService.deleteById(productId);

        // --- Send message to RabbitMQ ---
        productEventProducer.sendMessage(productId);

        return "Successfully deleted product with id " + productId;
    }

    /**
     * Cập nhật nhiều sản phẩm cùng lúc (batch), cộng dồn số lượng và kiểm tra không
     * bán âm.
     * type: "IMPORT" hoặc "SELL" (truyền từ controller)
     */
    public List<String> updateInventoryBatchWithType(List<ItemRequestDto> items, String username, String type)
            throws ExecutionException, InterruptedException {

        if (!List.of("IMPORT", "SELL").contains(type.toUpperCase())) {
            throw new IllegalArgumentException("Type must be either IMPORT or SELL");
        }

        Firestore db = FirestoreClient.getFirestore();
        List<String> results = new ArrayList<>();

        for (ItemRequestDto item : items) {
            try {
                DocumentReference productRef = db.collection(COLLECTION_NAME).document(item.getProductId());

                db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(productRef).get();
                    if (!snapshot.exists()) {
                        results.add("ERROR: Product " + item.getProductId() + " not found");
                        return null;
                    }

                    Product product = snapshot.toObject(Product.class);
                    if (product == null || product.getVariants() == null) {
                        results.add("ERROR: Product " + item.getProductId() + " data invalid");
                        return null;
                    }

                    Variant variant = product.getVariants().stream()
                            .filter(v -> v.getVariantId().equals(item.getVariantId()))
                            .findFirst()
                            .orElse(null);

                    if (variant == null) {
                        results.add("ERROR: Variant " + item.getVariantId() + " not found");
                        return null;
                    }

                    Long quantity = item.getQuantity();

                    if ("IMPORT".equalsIgnoreCase(type)) {
                        Long importQty = (variant.getQuantityImport() != null ? variant.getQuantityImport() : 0L);
                        variant.setQuantityImport(importQty + quantity);
                    } else {
                        Long sellQty = (variant.getQuantitySell() != null ? variant.getQuantitySell() : 0L);
                        Long importQty = variant.getQuantityImport() != null ? variant.getQuantityImport() : 0L;

                        // kiểm tra tồn kho hiện tại (import - sell)
                        Long currentStock = importQty - sellQty;

                        if (currentStock < quantity) {
                            results.add("ERROR: Not enough stock for product " + item.getProductId() +
                                    " variant " + item.getVariantId() + ". Available: " + currentStock);
                            return null;
                        }

                        variant.setQuantitySell(sellQty + quantity);
                    }

                    product.setUpdatedAt(Timestamp.now());
                    product.setUpdatedBy(username);

                    transaction.set(productRef, product, SetOptions.merge());
                    results.add("SUCCESS: " + type + " " + quantity + " units for product " +
                            item.getProductId() + " variant " + item.getVariantId());

                    return null;
                }).get();

            } catch (Exception e) {
                results.add("ERROR: Exception for product " + item.getProductId() +
                        " variant " + item.getVariantId() + " - " + e.getMessage());
            }
        }

        return results;
    }

    // SEARCH PRODUCT
    public List<ProductInforResponseDto> searchProduct(String keyword)
            throws ExecutionException, InterruptedException {

        keyword = removeVietnameseDiacritics(keyword);
        // search từ Elasticsearch
        List<ProductDocument> docs =
                productSearchService.searchByName(keyword);

        // nếu ES down hoặc không có kết quả
        if (docs.isEmpty()) {
            return new ArrayList<>();
        }

        Firestore db = FirestoreClient.getFirestore();
        List<ProductInforResponseDto> result = new ArrayList<>();

        // lấy dữ liệu đầy đủ từ Firestore
        for (ProductDocument doc : docs) {

            DocumentSnapshot snapshot = db.collection("products")
                    .document(doc.getProductId())
                    .get()
                    .get();

            if (snapshot.exists()) {
                Product product = snapshot.toObject(Product.class);
                if (product != null && "ACTIVE".equals(product.getStatus())) {
                    result.add(
                            productMapper.toProductInforResponseDto(product)
                    );
                }
            }
        }

        return result;
    }

}