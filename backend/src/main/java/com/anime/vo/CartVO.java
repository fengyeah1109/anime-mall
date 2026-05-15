package com.anime.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartVO {
    private Long id;
    private Long userId;
    private Long productId;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
    private Integer selected;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private BigDecimal totalPrice;
}
