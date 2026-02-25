package com.hmkeyewear.cart_service.service;

import com.google.api.core.ApiFuture;
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
import com.hmkeyewear.common_dto.dto.DiscountDto;
import com.hmkeyewear.common_dto.dto.OrderDetailRequestDto;
import com.hmkeyewear.common_dto.dto.OrderRequestDto;
import com.hmkeyewear.common_dto.dto.OrderResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.hmkeyewear.common_dto.dto.PaymentRequestDto;
import com.hmkeyewear.common_dto.dto.VNPayResponseDto;

import jakarta.servlet.http.HttpServletRequest;

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

        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Số lượng sản phẩm phải lớn hơn 0");
        }

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userId);

        Cart cart = db.runTransaction(transaction -> {

            DocumentSnapshot snapshot = transaction.get(docRef).get();
            Cart currentCart;

            // Nếu chưa có cart -> tạo mới trong memory
            if (!snapshot.exists()) {
                currentCart = new Cart();
                currentCart.setUserId(userId);
                currentCart.setItems(new ArrayList<>());
            } else {
                currentCart = snapshot.toObject(Cart.class);
                if (currentCart == null) {
                    currentCart = new Cart();
                    currentCart.setUserId(userId);
                    currentCart.setItems(new ArrayList<>());
                }
            }

            // Kiểm tra item đã tồn tại chưa
            Optional<CartItem> existingItem = currentCart.getItems().stream()
                    .filter(i ->
                            i.getProductId().equals(request.getProductId())
                                    && i.getVariantId().equals(request.getVariantId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(
                        existingItem.get().getQuantity() + request.getQuantity());
            } else {
                CartItem newItem = new CartItem(
                        request.getProductId(),
                        request.getVariantId(),
                        request.getColor(),
                        request.getProductName(),
                        request.getUnitPrice(),
                        request.getQuantity(),
                        request.getThumbnail()
                );
                currentCart.getItems().add(newItem);
            }

            // Recalculate total
            double total = currentCart.getItems().stream()
                    .mapToDouble(i -> i.getUnitPrice() * i.getQuantity())
                    .sum();

            currentCart.setTotal(total);

            // Save atomically
            transaction.set(docRef, currentCart);

            return currentCart;

        }).get();

        // 5Gửi event sau khi transaction thành công
        cartEventProducer.sendMessage(cart.toString());

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
        cartEventProducer.sendMessage(cart.toString());

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
        cartEventProducer.sendMessage(cart.toString());

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
            throw new RuntimeException("Không tìm thấy giỏ hàng người dùng có mã: " + userId);
        }

        Cart cart = snapshot.toObject(Cart.class);
        assert cart != null;
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang rỗng");
        }

        Optional<CartItem> optionalItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId) &&
                        i.getVariantId().equals(variantId))
                .findFirst();

        if (optionalItem.isEmpty()) {
            throw new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng: " + productId);
        }

        CartItem item = optionalItem.get();

        // Nếu có action: increment/decrement
        if (action != null) {
            switch (action.toLowerCase()) {
                case "increment" -> item.setQuantity(item.getQuantity() + 1);
                case "decrement" -> item.setQuantity(Math.max(item.getQuantity() - 1, 1)); // tối thiểu 1
                default -> throw new RuntimeException("Hành động không hợp lệ. Sử dụng 'increment' hoặc 'decrement'");
            }
        }

        // Nếu có quantity cụ thể
        if (quantity != null) {
            if (quantity <= 0)
                throw new RuntimeException("Số lượng phải > 0");
            item.setQuantity(quantity);
        }

        // Cập nhật tổng giá trị
        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getUnitPrice() * i.getQuantity())
                .sum();
        cart.setTotal(total);

        docRef.set(cart).get();

        // --- Send message to RabbitMQ ---
        cartEventProducer.sendMessage(cart.toString());

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
        cartEventProducer.sendMessage(cart.toString());

        return cartMapper.toResponseDto(cart);
    }

    public String deleteCart(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userId);
        ApiFuture<WriteResult> writeResult = docRef.delete();
        writeResult.get();

        // --- Send message to RabbitMQ ---
        cartEventProducer.sendMessage(userId);

        return "Xóa giỏ hàng thành công";
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
            throw new RuntimeException("Không thể gửi yêu cầu thanh toán lúc này");
        }

        return response;
    }

    public OrderResponseDto checkout(
            String userId,
            String emailFromHeader,
            CheckoutRequestDto checkoutRequest,
            HttpServletRequest request)
            throws ExecutionException, InterruptedException {

        // 1. Lấy cart theo userId
        CartResponseDto cart = getCart(userId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống, không thể thanh toán");
        }

        // 2. Chuẩn hóa CartItem -> OrderDetailRequestDto
        List<OrderDetailRequestDto> orderDetails = cart.getItems().stream()
                .map(item -> {
                    OrderDetailRequestDto detail = new OrderDetailRequestDto();
                    detail.setProductId(item.getProductId());
                    detail.setVariantId(item.getVariantId());
                    detail.setProductName(item.getProductName());
                    detail.setUnitPrice(item.getUnitPrice());
                    detail.setQuantity(item.getQuantity());
                    return detail;
                })
                .toList();

        // 3. Build OrderRequestDto gửi sang order-service
        OrderRequestDto orderRequest = new OrderRequestDto();
        orderRequest.setUserId(userId);
        orderRequest.setEmail(emailFromHeader);
        orderRequest.setFullName(checkoutRequest.getFullName());
        orderRequest.setPhone(checkoutRequest.getPhone());
        orderRequest.setPaymentMethod(checkoutRequest.getPaymentMethod());
        orderRequest.setNote(checkoutRequest.getNote());
        orderRequest.setDetails(orderDetails);
        orderRequest.setShip(checkoutRequest.getShip());

        // 5. Xử lý xác thực và gắn discount
        if (checkoutRequest.getDiscount() != null && checkoutRequest.getDiscount().getDiscountId() != null) {

            Discount discount = discountService.validateAndIncreaseUsage(checkoutRequest.getDiscount().getDiscountId());

            DiscountDto discountDto = new DiscountDto();
            discountDto.setDiscountId(discount.getDiscountId());
            discountDto.setValueType(discount.getValueType());
            discountDto.setValueDiscount(discount.getValueDiscount());

            orderRequest.setDiscount(discountDto);
        }

        // 6. Gửi message tạo order
        return (OrderResponseDto) orderCheckoutRequestEventProducer
                .convertSendAndReceive(orderRequest, userId);
    }

}
