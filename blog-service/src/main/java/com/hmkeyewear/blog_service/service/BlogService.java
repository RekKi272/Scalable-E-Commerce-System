package com.hmkeyewear.blog_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.blog_service.dto.BlogRequestDto;
import com.hmkeyewear.blog_service.dto.BlogResponseDto;
import com.hmkeyewear.blog_service.mapper.BlogMapper;
import com.hmkeyewear.blog_service.model.Blog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BlogService {
    @Autowired
    private BlogMapper blogMapper;

    private static final String COLLECTION_NAME = "blogs";
    private static final String COUNTER_COLLECTION = "counters";
    private static final String BLOG_COUNTER_DOC = "blogCounter";

    // GENERATE blogId
    private String generateProductId(Firestore db) throws ExecutionException, InterruptedException {
        DocumentReference counterRef = db.collection(COUNTER_COLLECTION).document(BLOG_COUNTER_DOC);

        ApiFuture<String> future = db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef).get();

            long lastId = 0;
            if (snapshot.exists() && snapshot.contains("lastId")) {
                lastId = snapshot.getLong("lastId");
            }

            long newId = lastId + 1;
            transaction.update(counterRef, "lastId", newId);

            // format: PROD0001, PROD0002
            return String.format("BLOG%03d", newId);
        });

        return future.get();
    }

    // CREATE BLOG
    public BlogResponseDto createBlog(String createdBy, BlogRequestDto blogRequestDto) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        Blog blog = blogMapper.toBlog(blogRequestDto);
        blog.setBlogId(generateProductId(db));
        blog.setCreatedAt(Timestamp.now());
        blog.setCreatedBy(createdBy);

        DocumentReference docRef = db.collection(COLLECTION_NAME).document(blog.getBlogId());
        ApiFuture<WriteResult> result = docRef.set(blog);
        result.get();

        return blogMapper.toBlogResponseDto(blog);
    }

    // Get blog by ID
    public BlogResponseDto getBlogById(String blogId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(blogId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot snapshot = future.get();
        if (snapshot.exists()) {
            Blog blog = snapshot.toObject(Blog.class);
            return blogMapper.toBlogResponseDto(blog);
        }
        return null;
    }

    // UPDATE Blog
    public BlogResponseDto updateBlog(String blogId, BlogRequestDto blogRequestDto) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(blogId);

        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) {
            return null;
        }

        Blog existingBlog = snapshot.toObject(Blog.class);
        assert existingBlog != null;
        existingBlog.setTitle(blogRequestDto.getTitle());
        existingBlog.setContent(blogRequestDto.getContent());
        existingBlog.setThumbnail(blogRequestDto.getThumbnail());
        existingBlog.setStatus(blogRequestDto.getStatus());

        ApiFuture<WriteResult> result = docRef.set(existingBlog);
        result.get();

        return blogMapper.toBlogResponseDto(existingBlog);
    }

    // DELETE blog
    public String deleteBlog(String blogId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(blogId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();

        return "Blog deleted successfully" + blogId;
    }

    // Get All Blog by status
    public List<BlogResponseDto> getAllActiveBlogs() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        // query tất cả document có status = "active"
        ApiFuture<QuerySnapshot> query = db.collection(COLLECTION_NAME)
                .whereEqualTo("status", "active")
                .get();

        List<QueryDocumentSnapshot> documents = query.get().getDocuments();

        List<BlogResponseDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            Blog blog = doc.toObject(Blog.class);
            result.add(blogMapper.toBlogResponseDto(blog));
        }

        return result;
    }

}
