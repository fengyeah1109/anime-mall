package com.anime.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartUpdateRequest {
    @NotNull
    private Long id;

    @NotNull
    @Min(1)
    private Integer quantity;

    private Integer selected; // 0/1
}

