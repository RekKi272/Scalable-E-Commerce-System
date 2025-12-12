package com.hmkeyewear.product_service.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Variant {
    private String variantId;
    private String color;
    private String thumbnail;

    private Long quantityImport = 0L;
    private Long quantitySell = 0L;
}
