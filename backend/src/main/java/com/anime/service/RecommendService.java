package com.anime.service;

import com.anime.entity.Product;

import java.util.List;

public interface RecommendService {

    /**
     * 为用户推荐商品
     * @param userId 用户ID
     * @param limit 推荐数量
     * @return 推荐商品列表
     */
    List<Product> recommendForUser(Long userId, int limit);

    /**
     * 获取热门和新品商品
     * @param limit 商品数量
     * @return 商品列表
     */
    List<Product> getHotAndNewProducts(int limit);

    /**
     * 获取销量前N的热门商品
     * @param limit 商品数量
     * @return 商品列表
     */
    List<Product> getHotProducts(int limit);
}
