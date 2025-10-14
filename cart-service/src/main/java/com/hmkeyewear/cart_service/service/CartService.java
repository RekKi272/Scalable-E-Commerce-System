package com.hmkeyewear.cart_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.cart_service.dto.CartRequestDto;
import com.hmkeyewear.cart_service.dto.CartResponseDto;
import com.hmkeyewear.cart_service.mapper.CartMapper;
import com.hmkeyewear.cart_service.model.Cart;
import com.hmkeyewear.cart_service.model.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class CartService {
    @Autowired
    CartMapper cartMapper;

    private static final String COLLECTION_NAME = "carts";

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

    public String deleteCart(String customerId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(customerId);
        ApiFuture<WriteResult> writeResult = docRef.delete();
        writeResult.get();
        return "Cart deleted successfully";
    }
}
