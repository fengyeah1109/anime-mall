package com.anime.service.impl;

import com.anime.entity.OrderItem;
import com.anime.mapper.OrderItemMapper;
import com.anime.service.OrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
}

