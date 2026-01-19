package com.hmkeyewear.product_service.repository;

import com.hmkeyewear.product_service.model.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, String> {

    Page<ProductDocument> findByProductNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    Page<ProductDocument> findByBrandIdAndStatus(
            String brandId,
            String status,
            Pageable pageable
    );
}
