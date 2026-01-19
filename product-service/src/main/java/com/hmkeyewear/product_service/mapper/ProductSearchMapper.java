package com.hmkeyewear.product_service.mapper;

import com.hmkeyewear.product_service.model.ProductDocument;
import com.hmkeyewear.product_service.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductSearchMapper {

    public ProductDocument toDocument(Product product) {
        ProductDocument doc = new ProductDocument();
        doc.setProductId(product.getProductId());
        doc.setProductName(product.getProductName());
        doc.setThumbnail(product.getThumbnail());
        doc.setBrandId(product.getBrandId());
        doc.setCategoryId(product.getCategoryId());
        doc.setSellingPrice(product.getSellingPrice());
        doc.setStatus(product.getStatus());
        doc.setCreatedAt(product.getCreatedAt().toDate().getTime());
        return doc;
    }
}
