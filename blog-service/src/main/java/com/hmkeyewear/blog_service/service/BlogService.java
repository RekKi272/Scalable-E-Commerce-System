package com.hmkeyewear.blog_service.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.hmkeyewear.blog_service.dto.BannerResponseDto;
import com.hmkeyewear.blog_service.dto.BlogRequestDto;
import com.hmkeyewear.blog_service.dto.BlogResponseDto;
import com.hmkeyewear.blog_service.mapper.BlogMapper;
import com.hmkeyewear.blog_service.messaging.BlogEventProducer;
import com.hmkeyewear.blog_service.model.Blog;
import com.hmkeyewear.common_dto.dto.PageResponseDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class BlogService {

    private final BlogMapper blogMapper;

    private final BlogEventProducer blogEventProducer;

    // Constructor
    public BlogService(BlogMapper blogMapper, BlogEventProducer blogEventProducer) {
        this.blogMapper = blogMapper;
        this.blogEventProducer = blogEventProducer;
    }

    private static final String COLLECTION_NAME = "blogs";
    private static final String COUNTER_COLLECTION = "counters";
    private static final String BLOG_COUNTER_DOC = "blogCounter";

    // GENERATE blogId
    private String generatBlogId(Firestore db) throws ExecutionException, InterruptedException {
        DocumentReference counterRef = db.collection(COUNTER_COLLECTION).document(BLOG_COUNTER_DOC);

        ApiFuture<String> future = db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef).get();

            long lastId = 0;
            if (snapshot.exists() && snapshot.contains("lastId")) {
                lastId = snapshot.getLong("lastId");
            }

            long newId = lastId + 1;
            transaction.set(counterRef, Map.of("lastId", newId), SetOptions.merge());

            // Format đúng: POST0001, POST0012, POST0123
            return String.format("BLOG%04d", newId);
        });

        return future.get();
    }

    // CREATE BLOG
    public BlogResponseDto createBlog(String createdBy, BlogRequestDto blogRequestDto)
            throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        Blog blog = blogMapper.toBlog(blogRequestDto);
        blog.setBlogId(generatBlogId(db));
        blog.setCreatedAt(Timestamp.now());
        blog.setCreatedBy(createdBy);

        DocumentReference docRef = db.collection(COLLECTION_NAME).document(blog.getBlogId());
        ApiFuture<WriteResult> result = docRef.set(blog);
        result.get();

        // --- Send message to RabbitMQ ---
        blogEventProducer.sendMessage(blog);

        return blogMapper.toBlogResponseDto(blog);
    }

    // Get All Blogs (kể cả inactive)
    public PageResponseDto<BlogResponseDto> getAllBlogs(int page, int size)
            throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        Query baseQuery = db.collection(COLLECTION_NAME)
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

        List<BlogResponseDto> items = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Blog blog = doc.toObject(Blog.class);
            items.add(blogMapper.toBlogResponseDto(blog));
        }

        return new PageResponseDto<>(
                items,
                page,
                size,
                totalElements,
                totalPages);
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
    public BlogResponseDto updateBlog(String blogId, BlogRequestDto blogRequestDto, String updatedBy)
            throws ExecutionException, InterruptedException {

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

        // Set updated info
        existingBlog.setUpdatedBy(updatedBy);
        existingBlog.setUpdatedAt(Timestamp.now());

        ApiFuture<WriteResult> result = docRef.set(existingBlog);
        result.get();

        // --- Send message to RabbitMQ ---
        blogEventProducer.sendMessage(existingBlog);

        return blogMapper.toBlogResponseDto(existingBlog);
    }

    // DELETE blog
    public String deleteBlog(String blogId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(blogId);
        ApiFuture<WriteResult> result = docRef.delete();
        result.get();

        // --- Send message to RabbitMQ ---
        blogEventProducer.sendMessage(blogId);

        return "Blog deleted successfully" + blogId;
    }

    // Get All Blog by status
    public PageResponseDto<BlogResponseDto> getAllActiveBlogs(int page, int size)
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

        List<BlogResponseDto> items = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            Blog blog = doc.toObject(Blog.class);
            items.add(blogMapper.toBlogResponseDto(blog));
        }

        return new PageResponseDto<>(
                items,
                page,
                size,
                totalElements,
                totalPages);
    }

}
