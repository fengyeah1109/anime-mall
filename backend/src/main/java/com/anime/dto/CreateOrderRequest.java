package com.anime.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long addressId;
    private Integer payType;
    private String remark;
}

