package com.hmkeyewear.user_service.service;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.common_dto.dto.PageResponseDto;
import com.hmkeyewear.user_service.dto.UserRequestDto;
import com.hmkeyewear.user_service.dto.UserResponseDto;
import com.hmkeyewear.user_service.mapper.UserMapper;
import com.hmkeyewear.user_service.messaging.UserEventProducer;
import com.hmkeyewear.user_service.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    private static final String COLLECTION_NAME = "users";

    private final UserMapper userMapper;

    private final UserEventProducer userEventProducer;

    public UserService(UserMapper userMapper, UserEventProducer userEventProducer) {
        this.userMapper = userMapper;
        this.userEventProducer = userEventProducer;
    }

    /** ==================== USER SELF ==================== */

    // Lấy thông tin user theo userId
    public UserResponseDto getUserById(String userId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(userId).get().get();

        if (!snapshot.exists()) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist");
        }
        User user = snapshot.toObject(User.class);
        if (user == null) {
            throw new IllegalStateException("User data corrupted or missing for ID " + userId);
        }
        return userMapper.toResponseDto(user);
    }

    // Cập nhật profile cá nhân (chỉ update các field cho phép)
    public UserResponseDto updateProfile(String userId, UserRequestDto dto)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userId);

        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist");
        }

        User existing = snapshot.toObject(User.class);
        if (existing == null) {
            throw new IllegalStateException("User data corrupted or missing for ID " + userId);
        }

        // Chỉ update các trường cho phép
        if (dto.getFirstName() != null)
            existing.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)
            existing.setLastName(dto.getLastName());
        if (dto.getGender() != null)
            existing.setGender(dto.getGender());
        if (dto.getBirthday() != null)
            existing.setBirthday(dto.getBirthday());
        if (dto.getPhone() != null)
            existing.setPhone(dto.getPhone());
        if (dto.getAddressProvince() != null)
            existing.setAddressProvince(dto.getAddressProvince());
        if (dto.getAddressWard() != null)
            existing.setAddressWard(dto.getAddressWard());
        if (dto.getAddressDetail() != null)
            existing.setAddressDetail(dto.getAddressDetail());

        existing.setUpdatedAt(Timestamp.now());
        existing.setUpdatedBy(userId);

        docRef.set(existing, SetOptions.merge()).get();

        // --- Send message to RabbitMQ ---
        userEventProducer.sendMessage(existing);

        return userMapper.toResponseDto(existing);
    }

    /** ==================== ADMIN / MANAGER ==================== */

    // Lấy tất cả user
    public PageResponseDto<UserResponseDto> getAllUser(int page, int size)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        Query query = db.collection(COLLECTION_NAME)
                .offset(page * size)
                .limit(size);

        List<QueryDocumentSnapshot> docs = query.get().get().getDocuments();

        List<UserResponseDto> items = docs.stream()
                .map(d -> userMapper.toResponseDto(d.toObject(User.class)))
                .toList();

        long totalElements = db.collection(COLLECTION_NAME)
                .get()
                .get()
                .size();

        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new PageResponseDto<>(
                items,
                page,
                size,
                totalElements,
                totalPages);
    }

    // Lấy user theo role
    public PageResponseDto<UserResponseDto> getUserByRole(String role, int page, int size)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        Query baseQuery = db.collection(COLLECTION_NAME)
                .whereEqualTo("role", role);

        Query pagedQuery = baseQuery
                .offset(page * size)
                .limit(size);

        List<QueryDocumentSnapshot> docs = pagedQuery.get().get().getDocuments();

        List<UserResponseDto> items = docs.stream()
                .map(d -> userMapper.toResponseDto(d.toObject(User.class)))
                .toList();

        long totalElements = baseQuery
                .get()
                .get()
                .size();

        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new PageResponseDto<>(
                items,
                page,
                size,
                totalElements,
                totalPages);
    }

    // Lấy user theo phone
    public UserResponseDto getUserByPhone(String phone) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<QueryDocumentSnapshot> docs = db.collection(COLLECTION_NAME)
                .whereEqualTo("phone", phone)
                .get()
                .get()
                .getDocuments();

        if (docs.isEmpty()) {
            throw new IllegalArgumentException("User with phone " + phone + " does not exist");
        }

        User user = docs.get(0).toObject(User.class);

        return userMapper.toResponseDto(user);
    }

    // Cập nhật user theo userId (admin)
    public UserResponseDto updateUser(String userId, UserRequestDto dto, String updatedBy)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userId);

        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist");
        }
        User existing = snapshot.toObject(User.class);
        if (existing == null) {
            throw new IllegalStateException("User data corrupted or missing for ID " + userId);
        }

        // Update tất cả field ngoại trừ meta
        if (dto.getFirstName() != null)
            existing.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)
            existing.setLastName(dto.getLastName());
        if (dto.getGender() != null)
            existing.setGender(dto.getGender());
        if (dto.getBirthday() != null)
            existing.setBirthday(dto.getBirthday());
        if (dto.getPhone() != null)
            existing.setPhone(dto.getPhone());
        if (dto.getEmail() != null)
            existing.setEmail(dto.getEmail());
        if (dto.getAddressProvince() != null)
            existing.setAddressProvince(dto.getAddressProvince());
        if (dto.getAddressWard() != null)
            existing.setAddressWard(dto.getAddressWard());
        if (dto.getAddressDetail() != null)
            existing.setAddressDetail(dto.getAddressDetail());
        if (dto.getRole() != null)
            existing.setRole(dto.getRole());
        if (dto.getStatus() != null)
            existing.setStatus(dto.getStatus());
        if (dto.getStoreId() != null)
            existing.setStoreId(dto.getStoreId());

        existing.setUpdatedAt(Timestamp.now());
        existing.setUpdatedBy(updatedBy);

        docRef.set(existing, SetOptions.merge()).get();

        // --- Send message to RabbitMQ ---
        userEventProducer.sendMessage(existing);

        return userMapper.toResponseDto(existing);
    }
}
