package com.anime.service.impl;

import com.anime.entity.Cart;
import com.anime.mapper.CartMapper;
import com.anime.service.CartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {
}

