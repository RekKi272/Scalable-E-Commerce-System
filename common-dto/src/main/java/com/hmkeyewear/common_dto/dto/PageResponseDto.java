package com.hmkeyewear.common_dto.dto;

import java.util.List;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDto<T> {

    private List<T> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
