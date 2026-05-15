package com.anime.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartSelectedRequest {
    @NotNull
    private Long id;

    private Integer selected; // 0/1
}

