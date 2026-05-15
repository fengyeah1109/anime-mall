package com.anime.service.impl;

import com.anime.entity.ArticleComment;
import com.anime.mapper.ArticleCommentMapper;
import com.anime.service.ArticleCommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ArticleCommentServiceImpl extends ServiceImpl<ArticleCommentMapper, ArticleComment> implements ArticleCommentService {
}

