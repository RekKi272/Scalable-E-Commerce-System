package com.hmkeyewear.auth_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.auth_service.dto.LoginRequestDto;
import com.hmkeyewear.auth_service.dto.RegisterCustomerRequestDto;
import com.hmkeyewear.auth_service.dto.AuthResponseDto;
import com.hmkeyewear.auth_service.dto.RegisterStaffRequestDto;
import com.hmkeyewear.auth_service.messaging.OtpEventProducer;
import com.hmkeyewear.auth_service.messaging.UserEventProducer;
import com.hmkeyewear.auth_service.model.PasswordResetToken;
import com.hmkeyewear.auth_service.model.User;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
@AllArgsConstructor
public class AuthService {

    private static final String COLLECTION_NAME = "users";

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserEventProducer userEventProducer;
    private final OtpEventProducer otpEventProducer;
    private final RefreshTokenRedisService refreshTokenRedisService;
    private final OtpService otpService;


    public String generateToken(String userId, String email, String role, String storeId) {
        return jwtService.generateAccessToken(userId, email, role, storeId);
    }

    public void validateToken(String token) {
        jwtService.validateToken(token);
    }

    // Đăng ký khách hàng
    public AuthResponseDto registerCustomer(RegisterCustomerRequestDto dto)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Kiểm tra email đã tồn tại chưa
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME)
                .whereEqualTo("email", dto.getEmail()).get();

        if (!query.get().isEmpty()) {
            throw new RuntimeException("Email already exists");
        }

        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        User user = new User();
        user.setUserId(docRef.getId());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setRole("ROLE_CUSTOMER");
        user.setStatus("ACTIVE");
        user.setCreatedAt(Timestamp.now());
        user.setCreatedBy(user.getUserId());

        docRef.set(user).get();

        // --- Send message to RabbitMQ ---
        userEventProducer.sendMessage(user);

        String accessToken = jwtService.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole(),
                null);

        String refreshToken = refreshTokenRedisService.create(user.getUserId());

        return new AuthResponseDto(
                user.getUserId(),
                user.getEmail(),
                user.getRole(),
                accessToken,
                refreshToken,
                user.getFirstName(),
                user.getLastName());
    }

    // Đăng ký nhân viên
    public AuthResponseDto registerStaff(RegisterStaffRequestDto dto, String creatBy)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Kiểm tra email đã tồn tại chưa
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME)
                .whereEqualTo("email", dto.getEmail()).get();

        if (!query.get().isEmpty()) {
            throw new RuntimeException("Email already exists");
        }

        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        User user = new User();
        user.setUserId(docRef.getId());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setGender(dto.getGender());
        user.setBirthday(dto.getBirthday());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAddressProvince(dto.getAddressProvince());
        user.setAddressWard(dto.getAddressWard());
        user.setAddressDetail(dto.getAddressDetail());

        user.setPassword(passwordEncoder.encode("123456"));
        user.setStoreId(dto.getStoreId());
        user.setRole(dto.getRole());
        user.setStatus("ACTIVE");
        user.setCreatedAt(Timestamp.now());
        user.setCreatedBy(creatBy);

        docRef.set(user).get();

        // --- Send message to RabbitMQ ---
        userEventProducer.sendMessage(user);

        String accessToken = jwtService.generateAccessToken(user.getUserId(), user.getEmail(), user.getRole(), user.getStoreId());

        String refreshToken = refreshTokenRedisService.create(user.getUserId());

        return new AuthResponseDto(
                user.getUserId(),
                user.getEmail(),
                user.getRole(),
                accessToken,
                refreshToken,
                user.getFirstName(),
                user.getLastName());
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
        User user = doc.toObject(User.class);

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole(),
                user.getStoreId());

        String refreshToken = refreshTokenRedisService.create(user.getUserId());

        return new AuthResponseDto(
                user.getUserId(),
                user.getEmail(),
                user.getRole(),
                accessToken,
                refreshToken,
                user.getFirstName(),
                user.getLastName());

    }

    // Tìm user theo email
    public Optional<User> findByEmail(String email) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .get();
        QuerySnapshot snapshot = query.get();

        if (snapshot.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        DocumentSnapshot doc = snapshot.getDocuments().get(0);
        User user = doc.toObject(User.class);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return Optional.of(user);
    }

    // Find User by Id
    public User findById(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get();

        QuerySnapshot snapshot = query.get();

        if (snapshot.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        DocumentSnapshot doc = snapshot.getDocuments().get(0);
        return doc.toObject(User.class);
    }

    // FORGOT PASSWORD PROCESSING
    public void forgotPasswordRequest(String email){
        // Get User
        Optional<User> optionalUser;
        try {
            optionalUser = findByEmail(email);
        } catch (Exception e) {
            return; // always return OK
        }

        if (optionalUser.isEmpty()) {
            return;
        }

        String otp = otpService.generateOtp(email);
        otpEventProducer.sendOtp(email, otp);
    }

    // RESET PASSWORD
    public void resetPassword(String email, String newPassword) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // Find user by Email
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME)
                .whereEqualTo("email", email)
                .get();

        QuerySnapshot snapshot = query.get();
        if (snapshot.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        DocumentSnapshot doc = snapshot.getDocuments().get(0);
        DocumentReference userRef = doc.getReference();

        // Encode new password
        String encodedPassword = passwordEncoder.encode(newPassword);

        // Update password
        userRef.update(
                "password", encodedPassword,
                "updatedAt", Timestamp.now()
        ).get();
    }
}
