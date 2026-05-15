package com.anime.service.impl;

import com.anime.entity.Order;
import com.anime.mapper.OrderMapper;
import com.anime.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
}

