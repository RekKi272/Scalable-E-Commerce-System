package com.hmkeyewear.product_service.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class Variant {
    private String variantId;
    private String color;
    private String thumbnail;
    private int quantity;
    // Danh sách ảnh của variant
    private List<Image> images;
}
