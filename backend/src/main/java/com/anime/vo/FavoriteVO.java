package com.anime.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteVO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private LocalDateTime createTime;
}
