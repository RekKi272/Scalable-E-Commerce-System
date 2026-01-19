package com.hmkeyewear.product_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products", createIndex = false)
public class ProductDocument {
    @Id
    private String productId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String productName;

    @Field(type = FieldType.Keyword)
    private String thumbnail;

    @Field(type = FieldType.Keyword)
    private String brandId;

    @Field(type = FieldType.Keyword)
    private String categoryId;

    @Field(type = FieldType.Double)
    private Double sellingPrice;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Long)
    private Long createdAt;
}
