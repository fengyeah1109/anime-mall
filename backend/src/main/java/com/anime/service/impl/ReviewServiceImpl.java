package com.anime.service.impl;

import com.anime.entity.Review;
import com.anime.mapper.ReviewMapper;
import com.anime.service.ReviewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {
}

