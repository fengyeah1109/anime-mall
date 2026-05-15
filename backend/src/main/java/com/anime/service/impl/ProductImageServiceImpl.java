package com.anime.service.impl;

import com.anime.entity.ProductImage;
import com.anime.mapper.ProductImageMapper;
import com.anime.service.ProductImageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ProductImageServiceImpl extends ServiceImpl<ProductImageMapper, ProductImage> implements ProductImageService {
}

