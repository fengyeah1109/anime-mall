package com.anime.service.impl;

import com.anime.entity.ProductCategory;
import com.anime.mapper.ProductCategoryMapper;
import com.anime.service.ProductCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {
}

