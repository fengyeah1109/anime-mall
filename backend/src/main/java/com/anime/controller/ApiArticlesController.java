package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.Article;
import com.anime.service.ArticleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ApiArticlesController {

    private final ArticleService articleService;

    @GetMapping
    public Result<Page<Article>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category
    ) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1);
        if (category != null && !category.isBlank()) {
            wrapper.eq(Article::getCategory, category);
        }
        wrapper.orderByDesc(Article::getPublishTime);

        return Result.success(articleService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        return Result.success(articleService.getOne(
                new LambdaQueryWrapper<Article>().eq(Article::getId, id).eq(Article::getStatus, 1)
        ));
    }
}

