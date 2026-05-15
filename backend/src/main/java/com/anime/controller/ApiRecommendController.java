package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.Product;
import com.anime.security.SecurityUtils;
import com.anime.service.RecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
public class ApiRecommendController {

    @Autowired
    private RecommendService recommendService;

    /**
     * 获取推荐商品列表
     * @param limit 推荐数量，默认10
     * @return 推荐商品列表
     */
    @GetMapping
    public Result<List<Product>> getRecommendations(@RequestParam(defaultValue = "10") int limit) {
        // 获取当前用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 获取推荐商品
        List<Product> recommendations = recommendService.recommendForUser(userId, limit);
        
        return Result.success(recommendations);
    }
}