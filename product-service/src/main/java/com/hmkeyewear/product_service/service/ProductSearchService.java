package com.hmkeyewear.product_service.service;

import com.hmkeyewear.product_service.model.ProductDocument;
import com.hmkeyewear.product_service.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private final ProductSearchRepository productSearchRepository;

    // ---------- SAVE ----------
    public void save(ProductDocument document) {
        try {
            productSearchRepository.save(document);
        } catch (Exception e) {
            log.warn("Elasticsearch DOWN — skip save index");
        }
    }

    // ---------- DELETE ----------
    public void deleteById(String id) {
        try {
            productSearchRepository.deleteById(id);
        } catch (Exception e) {
            log.warn("Elasticsearch DOWN — skip delete index");
        }
    }

    // ---------- SEARCH ----------
    public List<ProductDocument> searchByName(String keyword) {
        try {
            return productSearchRepository.findByProductNameContainingIgnoreCase(
                    keyword,
                    PageRequest.of(0, 5) // limit 5
            ).getContent();
        } catch (Exception e) {
            log.warn("Elasticsearch DOWN — skip search");
            return Collections.emptyList();
        }
    }
}
