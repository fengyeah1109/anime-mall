package com.anime.vo;

import com.anime.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private String userPhone;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal freightAmount;
    private BigDecimal discountAmount;
    private Integer payType;
    private Integer status;
    private LocalDateTime paymentTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;
    private Long addressId;
    private String receiverName;
    private String receiverPhone;
    private String fullAddress;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<OrderItem> items;
}
