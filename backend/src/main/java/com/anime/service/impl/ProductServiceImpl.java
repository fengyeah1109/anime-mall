package com.anime.service.impl;

import com.anime.entity.Product;
import com.anime.mapper.ProductMapper;
import com.anime.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
}

