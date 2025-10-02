package com.hmkeyewear.user_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.user_service.dto.CustomerRequestDto;
import com.hmkeyewear.user_service.dto.CustomerResponseDto;
import com.hmkeyewear.user_service.mapper.CustomerMapper;
import com.hmkeyewear.user_service.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class CustomerService {

    private static final String COLLECTION_NAME = "customers";

    @Autowired
    private CustomerMapper customerMapper;

    // CREATE Customer
    public CustomerResponseDto createCustomer(CustomerRequestDto dto) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        DocumentReference docRef = db.collection(COLLECTION_NAME).document();

        Customer customer = customerMapper.toCustomer(dto);
        customer.setCustomerId(docRef.getId());
        customer.setCreatedAt(Timestamp.now());
        customer.setCreatedBy(customer.getCustomerId());

        ApiFuture<WriteResult> result = docRef.set(customer);
        result.get();

        return customerMapper.toResponseDto(customer);
    }

    // GET customer
    public CustomerResponseDto getCustomer(String customerId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot snapshot = db.collection(COLLECTION_NAME).document(customerId).get().get();
        if (snapshot.exists()) {
            Customer customer = snapshot.toObject(Customer.class);
            return customerMapper.toResponseDto(customer);
        }
        return null;
    }

    // UPDATE customer
    public CustomerResponseDto updateCustomer(String customerId, String updatedBy, CustomerRequestDto dto) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(customerId);

        Customer customer = customerMapper.toCustomer(dto);
        customer.setCustomerId(customerId);
        customer.setUpdatedAt(Timestamp.now());
        customer.setUpdatedBy(updatedBy);

        docRef.set(customer, SetOptions.merge()).get();

        return customerMapper.toResponseDto(customer);
    }

    // DELETE Customer
    public void deleteCustomer(String customerId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        db.collection(COLLECTION_NAME).document(customerId).delete().get();
    }
}
