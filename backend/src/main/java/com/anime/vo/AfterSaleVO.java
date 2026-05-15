package com.anime.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AfterSaleVO {
    private Long id;
    private Long userId;
    private Long orderItemId;
    private Integer type;
    private String reason;
    private String description;
    private String evidenceImages;
    private Integer status;
    private LocalDateTime applyTime;
    private LocalDateTime handleTime;
    private String reply;

    // 关联的商品信息
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private Long orderId;
    private String orderNo;
}
