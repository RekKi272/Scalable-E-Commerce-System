package com.hmkeyewear.product_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import com.hmkeyewear.common_dto.dto.PageResponseDto;
import com.hmkeyewear.product_service.dto.ProductInforResponseDto;
import com.hmkeyewear.product_service.dto.ProductRequestDto;
import com.hmkeyewear.product_service.dto.ProductResponseDto;
import com.hmkeyewear.product_service.mapper.ProductMapper;
import com.hmkeyewear.product_service.mapper.ProductSearchMapper;
import com.hmkeyewear.product_service.messaging.ProductEventProducer;
import com.hmkeyewear.product_service.model.Product;
import com.hmkeyewear.product_service.model.ProductDocument;
import com.hmkeyewear.product_service.model.Variant;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

        Product product = new Product();
        product.setProductId(newProductId);
        product.setBrandId(dto.getBrandId());
        product.setCategoryId(dto.getCategoryId());
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setStatus(dto.getStatus());
        product.setThumbnail(dto.getThumbnail());
        product.setImportPrice(dto.getImportPrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setAttributes(dto.getAttributes());
        product.setImages(dto.getImages());

        product.setVariants(new ArrayList<>()); // khởi tạo rỗng

        product.setCreatedAt(Timestamp.now());
        product.setCreatedBy(userId);
        product.setUpdatedAt(Timestamp.now());
        product.setUpdatedBy(userId);

        db.collection(COLLECTION_NAME)
                .document(product.getProductId())
                .set(product)
                .get();

        ProductDocument doc = productSearchMapper.toDocument(product);
        doc.setProductName(removeVietnameseDiacritics(product.getProductName()));
        productSearchService.save(doc);

        productEventProducer.sendMessage(product);

        return productMapper.toProductResponseDto(product);
    }

    // Khi tạo đơn hàng thành công -> cộng số lượng đã bán
    public void increaseQuantitySell(List<OrderDetailRequestDto> items) {
        updateQuantitySell(items, true);
    }

    // Khi đơn bị huỷ / thanh toán thất bại -> trừ lại số lượng đã bán
    public void decreaseQuantitySell(List<OrderDetailRequestDto> items) {
        updateQuantitySell(items, false);
    }

    private void updateQuantitySell(
            List<OrderDetailRequestDto> items,
            boolean isIncrease) {
        Firestore db = FirestoreClient.getFirestore();

        // Gom theo productId
        Map<String, List<OrderDetailRequestDto>> grouped = items.stream()
                .collect(Collectors.groupingBy(OrderDetailRequestDto::getProductId));

        // Mỗi product chạy 1 transaction
        for (String productId : grouped.keySet()) {

            db.runTransaction(transaction -> {
                DocumentReference productRef = db.collection(COLLECTION_NAME).document(productId);

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
                            .orElseThrow(() -> new RuntimeException(
                                    "Variant " + detail.getVariantId()
                                            + " not found in product " + productId));

                    long currentSell = variant.getQuantitySell() != null
                            ? variant.getQuantitySell()
                            : 0L;

                    long newSell;

                    if (isIncrease) {
                        // BÁN HÀNG
                        newSell = currentSell + detail.getQuantity();
                    } else {
                        // HUỶ / FAIL -> TRỪ LẠI
                        newSell = currentSell - detail.getQuantity();
                        if (newSell < 0) {
                            newSell = 0L; // bảo vệ dữ liệu
                        }
                    }

                    variant.setQuantitySell(newSell);
                }

                // Lưu lại variants
                transaction.update(productRef, "variants", variants);
                return null;
            });
        }
    }

    // REFUND / CANCEL ORDER -> return stock
    public void refundStock(List<OrderDetailRequestDto> items) {
        Firestore db = FirestoreClient.getFirestore();

        // Gom item theo productId
        Map<String, List<OrderDetailRequestDto>> grouped = items.stream()
                .collect(Collectors.groupingBy(OrderDetailRequestDto::getProductId));

        for (String productId : grouped.keySet()) {
            db.runTransaction(transaction -> {

                DocumentReference productRef = db.collection(COLLECTION_NAME).document(productId);

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
                                        " not found in product " + productId);
                    }

                    Long sellQty = variant.getQuantitySell() != null
                            ? variant.getQuantitySell()
                            : 0L;

                    // không cho refund vượt quá số đã bán
                    if (sellQty < detail.getQuantity()) {
                        throw new RuntimeException(
                                "Refund quantity exceeds sold quantity for variant "
                                        + variant.getVariantId()
                                        + " (sold: " + sellQty
                                        + ", refund: " + detail.getQuantity() + ")");
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
    @Cacheable(value = "product", key = "'product:' + #p0", unless = "#result == null")
    public ProductResponseDto getProductById(String productId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(productId).get().get();

        if (snapshot.exists()) {
            Product product = snapshot.toObject(Product.class);
            if (product == null) {
                throw new RuntimeException("Dữ liệu sản phẩm có" + productId + " rỗng");
            }
            return productMapper.toProductResponseDto(product);
        }
        return null;
    }

    // GET All Products
    public PageResponseDto<ProductResponseDto> getAllProducts(int page, int size)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        Query baseQuery = db.collection(COLLECTION_NAME)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        // lấy tổng số phần tử
        long totalElements = baseQuery.get().get().size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        Query pageQuery = baseQuery.limit(size);

        if (page > 0) {
            QuerySnapshot prevSnapshot = baseQuery
                    .limit(page * size)
                    .get()
                    .get();

            if (!prevSnapshot.isEmpty()) {
                DocumentSnapshot lastDoc = prevSnapshot.getDocuments().get(prevSnapshot.size() - 1);
                pageQuery = pageQuery.startAfter(lastDoc);
            }
        }

        QuerySnapshot snapshot = pageQuery.get().get();

        List<ProductResponseDto> items = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Product product = doc.toObject(Product.class);
            items.add(productMapper.toProductResponseDto(product));
        }

        return new PageResponseDto<>(
                items,
                page,
                size,
                totalElements,
                totalPages);
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
    @CacheEvict(value = "product", key = "'product:' + #p0")
    public ProductResponseDto updateProduct(String productId, ProductRequestDto dto, String userId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference ref = db.collection(COLLECTION_NAME).document(productId);

        DocumentSnapshot snapshot = ref.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Không tìm thấy sản phẩm");
        }

        Product existing = snapshot.toObject(Product.class);

        Product updated = productMapper.toProduct(dto);
        updated.setProductId(productId);
        updated.setVariants(existing.getVariants()); // GIỮ NGUYÊN
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setCreatedBy(existing.getCreatedBy());
        updated.setUpdatedAt(Timestamp.now());
        updated.setUpdatedBy(userId);

        ref.set(updated).get();

        ProductDocument doc = productSearchMapper.toDocument(updated);
        doc.setProductName(removeVietnameseDiacritics(updated.getProductName()));
        productSearchService.save(doc);

        productEventProducer.sendMessage(updated);

        return productMapper.toProductResponseDto(updated);
    }

    // GET Active Product ONLY
    @Cacheable(value = "active_products", key = "'page:' + #page + ':size:' + #size")
    public PageResponseDto<ProductInforResponseDto> getActiveProducts(int page, int size)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        Query baseQuery = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        long totalElements = baseQuery.get().get().size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        Query pageQuery = baseQuery.limit(size);

        if (page > 0) {
            QuerySnapshot prevSnapshot = baseQuery
                    .limit(page * size)
                    .get()
                    .get();

            if (!prevSnapshot.isEmpty()) {
                DocumentSnapshot lastDoc = prevSnapshot.getDocuments().get(prevSnapshot.size() - 1);
                pageQuery = pageQuery.startAfter(lastDoc);
            }
        }

        QuerySnapshot snapshot = pageQuery.get().get();

        List<ProductInforResponseDto> items = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Product product = doc.toObject(Product.class);
            items.add(productMapper.toProductInforResponseDto(product));
        }

        return new PageResponseDto<>(
                items,
                page,
                size,
                totalElements,
                totalPages);
    }

    // DELETE Product
    @Caching(evict = {
            @CacheEvict(value = "product", key = "'product:' + #p0"),
            @CacheEvict(value = "active_products", allEntries = true)
    })
    public String deleteProduct(String productId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot snapshot = db.collection(COLLECTION_NAME)
                .document(productId)
                .get()
                .get();

        if (!snapshot.exists()) {
            throw new RuntimeException("Không tìm thấy sản phẩm");
        }

        Product product = snapshot.toObject(Product.class);

        boolean hasSoldVariant = product.getVariants() != null &&
                product.getVariants().stream()
                        .anyMatch(v -> v.getQuantitySell() != null && v.getQuantitySell() > 0);

        if (hasSoldVariant) {
            throw new RuntimeException("Không thể xóa sản phẩm vì đã có biến thể được bán");
        }

        db.collection(COLLECTION_NAME).document(productId).delete().get();
        productSearchService.deleteById(productId);
        productEventProducer.sendMessage(productId);

        return "Xóa sản phẩm " + productId;
    }

    // SEARCH PRODUCT
    public List<ProductInforResponseDto> searchProduct(String keyword)
            throws ExecutionException, InterruptedException {

        keyword = removeVietnameseDiacritics(keyword);
        // search từ Elasticsearch
        List<ProductDocument> docs = productSearchService.searchByName(keyword);

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
                            productMapper.toProductInforResponseDto(product));
                }
            }
        }

        return result;
    }

}