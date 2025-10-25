package com.hmkeyewear.auth_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.auth_service.dto.LoginRequestDto;
import com.hmkeyewear.auth_service.dto.AuthResponseDto;
import com.hmkeyewear.auth_service.dto.RegisterRequestDto;
import com.hmkeyewear.auth_service.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class AuthService {

    private static final String COLLECTION_NAME = "customers";

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthService(PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Extract role from email
    public String getRoleFromEmail(String email) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .get();

        QuerySnapshot snapshot = query.get();

        if (snapshot.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        DocumentSnapshot doc = snapshot.getDocuments().get(0);
        Customer customer = doc.toObject(Customer.class);

        return customer.getRole();
    }

    public String generateToken(String customerId, String email, String role, String storeId) {
        return jwtService.generateToken(customerId, email, role, storeId);
    }

    public void validateToken(String token) {
        jwtService.validateToken(token);
    }

    // Đăng ký tài khoản
    public AuthResponseDto register(RegisterRequestDto dto) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Kiểm tra email đã tồn tại chưa
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME)
                .whereEqualTo("email", dto.getEmail())
                .get();

        if (!query.get().isEmpty()) {
            throw new RuntimeException("Email already exists");
        }

        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        Customer customer = new Customer();
        customer.setCustomerId(docRef.getId());
        customer.setFirstName(dto.getFirstName());
        customer.setLastName(dto.getLastName());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        customer.setEmail(dto.getEmail());
        customer.setPassword(passwordEncoder.encode(dto.getPassword()));
        customer.setSex(dto.isSex());
        customer.setRole("USER");
        customer.setStatus("ACTIVE");
        customer.setCreatedAt(Timestamp.now());
        customer.setCreatedBy(customer.getCustomerId());
        customer.setStoreId(dto.getStoreId());

        ApiFuture<WriteResult> result = docRef.set(customer);
        result.get();

        // Tạo JWT sau khi đăng ký
        String token = jwtService.generateToken(customer.getCustomerId(), customer.getEmail(), customer.getRole(), customer.getStoreId());

        return new AuthResponseDto(customer.getCustomerId(), customer.getEmail(), customer.getRole(), token);
    }

    // Đăng nhập
    public AuthResponseDto login(LoginRequestDto dto) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME)
                .whereEqualTo("email", dto.getEmail())
                .get();

        QuerySnapshot snapshot = query.get();

        if (snapshot.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        DocumentSnapshot doc = snapshot.getDocuments().get(0);
        Customer customer = doc.toObject(Customer.class);

        if (!passwordEncoder.matches(dto.getPassword(), customer.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(customer.getCustomerId(), customer.getEmail(), customer.getRole(), customer.getStoreId());
        return new AuthResponseDto(customer.getCustomerId(), customer.getEmail(), customer.getRole(), token);
    }

    // Find By Email
    public Optional<Customer> findByEmail(String email) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .get();
        QuerySnapshot snapshot = query.get();
        if (snapshot.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        if (snapshot.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        DocumentSnapshot doc = snapshot.getDocuments().get(0);
        return Optional.of(doc.toObject(Customer.class));
    }
}
