package com.anime.service;

import com.anime.dto.CreateOrderRequest;
import com.anime.vo.OrderVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface OrderBizService {
    OrderVO createFromSelectedCart(Long userId, CreateOrderRequest request);
    void pay(Long userId, Long orderId);
    Page<OrderVO> myOrders(Long userId, Integer status, int page, int size);
    OrderVO myOrderDetail(Long userId, Long orderId);
    void cancel(Long userId, Long orderId);
    void receive(Long userId, Long orderId);
}

