package com.hmkeyewear.cart_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.cart_service.dto.*;
import com.hmkeyewear.cart_service.mapper.CartMapper;
import com.hmkeyewear.cart_service.messaging.CartEventProducer;
import com.hmkeyewear.cart_service.messaging.OrderCheckoutRequestEventProducer;
import com.hmkeyewear.cart_service.messaging.PaymentRequestEventProducer;
import com.hmkeyewear.cart_service.model.Cart;
import com.hmkeyewear.cart_service.model.CartItem;
import com.hmkeyewear.cart_service.model.Discount;
import com.hmkeyewear.common_dto.dto.OrderRequestDto;
import com.hmkeyewear.common_dto.dto.OrderResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.hmkeyewear.common_dto.dto.PaymentRequestDto;
import com.hmkeyewear.common_dto.dto.VNPayResponseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
@Slf4j
public class CartService {

    private final CartMapper cartMapper;

    private final CartEventProducer cartEventProducer;

    private final PaymentRequestEventProducer paymentRequestEventProducer;

    private final OrderCheckoutRequestEventProducer orderCheckoutRequestEventProducer;

    private final DiscountService discountService;

    private static final String COLLECTION_NAME = "carts";

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    public CartResponseDto addToCart(AddToCartRequestDto request, String userId)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userId);

        // Lấy giỏ hàng hiện tại
        DocumentSnapshot snapshot = docRef.get().get();
        Cart cart;

        if (!snapshot.exists()) {
            // Nếu chưa có giỏ hàng -> tạo mới
            CartRequestDto newCartDto = new CartRequestDto();
            newCartDto.setUserId(userId);
            newCartDto.setItems(new ArrayList<>());
            createCart(newCartDto); // gọi hàm createCart() để đảm bảo thống nhất cấu trúc
            cart = cartMapper.toCart(newCartDto);
        } else {
            cart = snapshot.toObject(Cart.class);
        }

        // Nếu vì lý do nào đó cart vẫn null thì tạo mới
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
        }

        // Kiểm tra sản phẩm đã có trong giỏ chưa
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()) &&
                        i.getVariantId().equals(request.getVariantId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = new CartItem(
                    request.getProductId(),
                    request.getVariantId(),
                    request.getProductName(),
                    request.getUnitPrice(),
                    request.getQuantity(),
                    request.getThumbnail());
            cart.getItems().add(newItem);
        }

        // Tính tổng tiền
        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getUnitPrice() * i.getQuantity())
                .sum();

        cart.setTotal(total);

        // Lưu lại vào Firestore
        ApiFuture<WriteResult> writeResult = docRef.set(cart);
        writeResult.get();

        // --- Send message to RabbitMQ ---
        cartEventProducer.sendMessage(cart);

        return cartMapper.toResponseDto(cart);
    }

    public CartResponseDto createCart(CartRequestDto dto) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(dto.getUserId());
        Cart cart = new Cart();
        cart.setUserId(dto.getUserId());
        cart.setItems(dto.getItems());

        double totalPrice = 0.0;
        for (CartItem cartItem : cart.getItems()) {
            totalPrice += cartItem.getUnitPrice() * cartItem.getQuantity();
        }
        cart.setTotal(totalPrice);

        ApiFuture<WriteResult> writeResult = docRef.set(cart);
        writeResult.get();

        // --- Send message to RabbitMQ ---
        cartEventProducer.sendMessage(cart);

        return cartMapper.toResponseDto(cart);
    }

    /**
     * Lấy giỏ hàng của người dùng
     */
    public CartResponseDto getCart(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userId);
        ApiFuture<DocumentSnapshot> docSnapshot = docRef.get();
        DocumentSnapshot snapshot = docSnapshot.get();

        if (snapshot.exists()) {
            Cart cart = snapshot.toObject(Cart.class);
            return cartMapper.toResponseDto(cart);
        }
        return null;
    }

    public CartResponseDto updateCart(String userId, CartRequestDto dto)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userId);

        Cart cart = new Cart();
        cart.setUserId(dto.getUserId());
        cart.setItems(dto.getItems());

        double totalPrice = 0.0;
        for (CartItem cartItem : cart.getItems()) {
            totalPrice += cartItem.getUnitPrice() * cartItem.getQuantity();
        }
        cart.setTotal(totalPrice);

        ApiFuture<WriteResult> writeResult = docRef.set(cart);
        writeResult.get();

        // --- Send message to RabbitMQ ---
        cartEventProducer.sendMessage(cart);

        return cartMapper.toResponseDto(cart);
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * Có thể tăng/giảm 1 đơn vị hoặc set số lượng cụ thể
     */
    public CartResponseDto updateItemQuantity(String userId, String productId, String variantId, String action,
            Integer quantity)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userId);

        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Cart not found for customer: " + userId);
        }

        Cart cart = snapshot.toObject(Cart.class);
        assert cart != null;
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Optional<CartItem> optionalItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId) &&
                        i.getVariantId().equals(variantId))
                .findFirst();

        if (optionalItem.isEmpty()) {
            throw new RuntimeException("Product not found in cart: " + productId);
        }

        CartItem item = optionalItem.get();

        // Nếu có action: increment/decrement
        if (action != null) {
            switch (action.toLowerCase()) {
                case "increment" -> item.setQuantity(item.getQuantity() + 1);
                case "decrement" -> item.setQuantity(Math.max(item.getQuantity() - 1, 1)); // tối thiểu 1
                default -> throw new RuntimeException("Invalid action. Use 'increment' or 'decrement'");
            }
        }

        // Nếu có quantity cụ thể
        if (quantity != null) {
            if (quantity <= 0)
                throw new RuntimeException("Quantity must be > 0");
            item.setQuantity(quantity);
        }

        // Cập nhật tổng giá trị
        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getUnitPrice() * i.getQuantity())
                .sum();
        cart.setTotal(total);

        docRef.set(cart).get();

        // --- Send message to RabbitMQ ---
        cartEventProducer.sendMessage(cart);

        return cartMapper.toResponseDto(cart);
    }

    /**
     * Xóa 1 item khỏi giỏ hàng
     */
    public CartResponseDto removeItem(String userId, String productId, String variantId)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userId);
        DocumentSnapshot snapshot = docRef.get().get();

        if (!snapshot.exists()) {
            throw new RuntimeException("Giỏ hàng không tồn tại");
        }

        Cart cart = snapshot.toObject(Cart.class);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống");
        }

        // Lọc bỏ item cần xóa
        List<CartItem> updatedItems = cart.getItems().stream()
                .filter(i -> !i.getProductId().equals(productId)
                        || (variantId != null && !variantId.equals(i.getVariantId())))
                .toList();

        cart.setItems(updatedItems);

        // Tính lại tổng
        double total = updatedItems.stream()
                .mapToDouble(i -> i.getUnitPrice() * i.getQuantity())
                .sum();
        cart.setTotal(total);

        // Lưu lại vào Firestore
        docRef.set(cart).get();

        // --- Gửi message RabbitMQ ---
        cartEventProducer.sendMessage(cart);

        return cartMapper.toResponseDto(cart);
    }

    public String deleteCart(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userId);
        ApiFuture<WriteResult> writeResult = docRef.delete();
        writeResult.get();

        // --- Send message to RabbitMQ ---
        cartEventProducer.sendMessage(userId);

        return "Cart deleted successfully";
    }

    /**
     * Xóa giỏ hàng (sau khi thanh toán)
     */
    public void clearCart(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(userId).delete();
    }

    public VNPayResponseDto createPayment(
            OrderResponseDto order,
            String ipAddress) throws ExecutionException, InterruptedException {

        // ===== BUILD PaymentRequestDto (CHỈ FIELD PAYMENT-SERVICE CẦN) =====
        PaymentRequestDto request = new PaymentRequestDto();

        request.setOrderId(order.getOrderId());
        request.setTotal(order.getSummary());
        request.setIpAddress(ipAddress);

        // ===== RPC CALL =====
        VNPayResponseDto response = paymentRequestEventProducer.sendPaymentRequest(request);

        if (response == null) {
            throw new RuntimeException("Failed to send payment request");
        }

        return response;
    }

    public CartResponseDto applyDiscount(String userId, String discountCode)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference cartRef = db.collection(COLLECTION_NAME).document(userId);
        DocumentSnapshot snapshot = cartRef.get().get();

        if (!snapshot.exists()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        Cart cart = snapshot.toObject(Cart.class);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        // --- Lấy danh sách discount từ Firestore ---
        List<Discount> allDiscounts = discountService.getAllDiscountsRaw(); // trả về Discount object
        Optional<Discount> optionalDiscount = allDiscounts.stream()
                .filter(d -> d.getDiscountId().equalsIgnoreCase(discountCode))
                .findFirst();

        if (optionalDiscount.isEmpty()) {
            throw new RuntimeException("Mã giảm giá không hợp lệ hoặc đã hết hạn");
        }

        Discount discount = optionalDiscount.get();

        Timestamp now = Timestamp.now();
        if (discount.getStartDate() != null && now.compareTo(discount.getStartDate()) < 0) {
            throw new RuntimeException("Mã giảm giá chưa bắt đầu");
        }
        if (discount.getEndDate() != null && now.compareTo(discount.getEndDate()) > 0) {
            throw new RuntimeException("Mã giảm giá đã hết hạn");
        }

        if (discount.getMaxQuantity() > 0 && discount.getUsedQuantity() >= discount.getMaxQuantity()) {
            throw new RuntimeException("Mã giảm giá đã đạt giới hạn sử dụng");
        }

        double totalBefore = cart.getTotal();

        if (discount.getMinPriceRequired() != null && totalBefore < discount.getMinPriceRequired()) {
            throw new RuntimeException("Giá trị giỏ hàng chưa đạt mức tối thiểu để áp dụng mã giảm giá");
        }

        // --- Tính toán discount ---
        double discountAmount = 0.0;
        if ("percentage".equalsIgnoreCase(discount.getValueType())) {
            discountAmount = totalBefore * discount.getValueDiscount() / 100.0;
            if (discount.getMaxPriceDiscount() != null && discountAmount > discount.getMaxPriceDiscount()) {
                discountAmount = discount.getMaxPriceDiscount();
            }
        } else if ("fixed".equalsIgnoreCase(discount.getValueType())) {
            discountAmount = discount.getValueDiscount();
            if (discountAmount > totalBefore)
                discountAmount = totalBefore; // không âm
        } else {
            throw new RuntimeException("Loại giảm giá không hợp lệ");
        }

        double newTotal = Math.max(totalBefore - discountAmount, 0.0);

        // --- Cập nhật cart ---
        cart.setDiscountId(discount.getDiscountId());
        // cart.setDiscountAmount(discountAmount);
        cart.setTotal(newTotal);

        cartRef.set(cart).get();

        // --- Cập nhật số lượng sử dụng của discount ---
        discount.setUsedQuantity(discount.getUsedQuantity() + 1);
        discountService.updateDiscountUsage(discount);

        // --- Gửi message RabbitMQ ---
        cartEventProducer.sendMessage(cart);

        return cartMapper.toResponseDto(cart);
    }

    public OrderResponseDto sendCreateOrder(OrderRequestDto orderRequest, String userId) {

        return (OrderResponseDto) orderCheckoutRequestEventProducer.convertSendAndReceive(orderRequest, userId);
    }

}
