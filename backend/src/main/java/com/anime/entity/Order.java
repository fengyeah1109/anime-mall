package com.anime.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
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
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

