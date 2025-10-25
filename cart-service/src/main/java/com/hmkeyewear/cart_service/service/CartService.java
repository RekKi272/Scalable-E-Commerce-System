package com.hmkeyewear.cart_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.cart_service.dto.AddToCartRequestDto;
import com.hmkeyewear.cart_service.dto.CartRequestDto;
import com.hmkeyewear.cart_service.dto.CartResponseDto;
import com.hmkeyewear.cart_service.mapper.CartMapper;
import com.hmkeyewear.cart_service.model.Cart;
import com.hmkeyewear.cart_service.model.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class CartService {
    @Autowired
    CartMapper cartMapper;

    private static final String COLLECTION_NAME = "carts";

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    public CartResponseDto addToCart(AddToCartRequestDto request, String customerId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(customerId);

        // Lấy giỏ hàng hiện tại
        DocumentSnapshot snapshot = docRef.get().get();
        Cart cart;

        if (!snapshot.exists()) {
            // Nếu chưa có giỏ hàng -> tạo mới
            CartRequestDto newCartDto = new CartRequestDto();
            newCartDto.setCustomerId(customerId);
            newCartDto.setItems(new ArrayList<>());
            createCart(newCartDto); // gọi hàm createCart() để đảm bảo thống nhất cấu trúc
            cart = cartMapper.toCart(newCartDto);
        } else {
            cart = snapshot.toObject(Cart.class);
        }

        // Nếu vì lý do nào đó cart vẫn null thì tạo mới
        if (cart == null) {
            cart = new Cart();
            cart.setCustomerId(customerId);
            cart.setItems(new ArrayList<>());
        }

        // Kiểm tra sản phẩm đã có trong giỏ chưa
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
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
                    request.getThumbnail()
            );
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

        return cartMapper.toResponseDto(cart);
    }


    public CartResponseDto createCart(CartRequestDto dto) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(dto.getCustomerId());
        Cart cart = new Cart();
        cart.setCustomerId(dto.getCustomerId());
        cart.setItems(dto.getItems());

        double totalPrice = 0.0;
        for (CartItem cartItem : cart.getItems()) {
            totalPrice += cartItem.getUnitPrice() * cartItem.getQuantity();
        }
        cart.setTotal(totalPrice);

        ApiFuture<WriteResult> writeResult = docRef.set(cart);
        writeResult.get();

        return cartMapper.toResponseDto(cart);
    }

    /**
     * Lấy giỏ hàng của người dùng
     */
    public CartResponseDto getCart(String customerId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(customerId);
        ApiFuture<DocumentSnapshot> docSnapshot = docRef.get();
        DocumentSnapshot snapshot = docSnapshot.get();

        if (snapshot.exists()) {
            Cart cart = snapshot.toObject(Cart.class);
            return cartMapper.toResponseDto(cart);
        }
        return null;
    }

    public CartResponseDto updateCart(String customerId, CartRequestDto dto)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(customerId);

        Cart cart = new Cart();
        cart.setCustomerId(dto.getCustomerId());
        cart.setItems(dto.getItems());

        double totalPrice = 0.0;
        for (CartItem cartItem : cart.getItems()) {
            totalPrice += cartItem.getUnitPrice() * cartItem.getQuantity();
        }
        cart.setTotal(totalPrice);

        ApiFuture<WriteResult> writeResult = docRef.set(cart);
        writeResult.get();
        return cartMapper.toResponseDto(cart);
    }

    /**
     *  Cập nhật số lượng sản phẩm trong giỏ hàng
     * Có thể tăng/giảm 1 đơn vị hoặc set số lượng cụ thể
     */
    public CartResponseDto updateItemQuantity(String customerId, String productId, String action, Integer quantity)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(customerId);

        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new RuntimeException("Cart not found for customer: " + customerId);
        }

        Cart cart = snapshot.toObject(Cart.class);
        assert cart != null;
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Optional<CartItem> optionalItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();

        if (optionalItem.isEmpty()) {
            throw new RuntimeException("Product not found in cart: " + productId);
        }

        CartItem item = optionalItem.get();

        //  Nếu có action: increment/decrement
        if (action != null) {
            switch (action.toLowerCase()) {
                case "increment" -> item.setQuantity(item.getQuantity() + 1);
                case "decrement" -> item.setQuantity(Math.max(item.getQuantity() - 1, 1)); // tối thiểu 1
                default -> throw new RuntimeException("Invalid action. Use 'increment' or 'decrement'");
            }
        }

        // Nếu có quantity cụ thể
        if (quantity != null) {
            if (quantity <= 0) throw new RuntimeException("Quantity must be > 0");
            item.setQuantity(quantity);
        }

        // Cập nhật tổng giá trị
        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getUnitPrice() * i.getQuantity())
                .sum();
        cart.setTotal(total);

        docRef.set(cart).get();
        return cartMapper.toResponseDto(cart);
    }



    public String deleteCart(String customerId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(customerId);
        ApiFuture<WriteResult> writeResult = docRef.delete();
        writeResult.get();
        return "Cart deleted successfully";
    }


    /**
     * Xóa giỏ hàng (sau khi thanh toán)
     */
    public void clearCart(String customerId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(customerId).delete();
    }
}
