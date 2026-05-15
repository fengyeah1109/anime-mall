package com.anime.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.anime.entity.Product;
import com.anime.entity.UserBehavior;
import com.anime.mapper.ProductMapper;
import com.anime.mapper.UserBehaviorMapper;
import com.anime.service.RecommendService;
import com.anime.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Autowired
    private UserBehaviorMapper userBehaviorMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public List<Product> recommendForUser(Long userId, int limit) {
        System.out.println("=== recommendForUser called, userId=" + userId + ", limit=" + limit);
        
        // 如果用户未登录或行为数据不足，返回热门+新品
        if (userId == null) {
            System.out.println("User not logged in, returning hot+new products");
            return getHotAndNewProducts(limit);
        }

        // 检查用户行为数量
        long behaviorCount = userBehaviorMapper.selectCount(
                new LambdaQueryWrapper<UserBehavior>().eq(UserBehavior::getUserId, userId)
        );
        System.out.println("User behavior count: " + behaviorCount);

        if (behaviorCount < 5) {
            System.out.println("Behavior count < 5, returning hot+new products");
            return getHotAndNewProducts(limit);
        }

        // 读取权重配置
        double cfWeight = systemConfigService.getConfigValueAsDouble("recommend.cf_weight", 0.4);
        double contentWeight = systemConfigService.getConfigValueAsDouble("recommend.content_weight", 0.4);
        double hotWeight = systemConfigService.getConfigValueAsDouble("recommend.hot_weight", 0.2);
        System.out.println("Weights - CF: " + cfWeight + ", Content: " + contentWeight + ", Hot: " + hotWeight);

        // 获取用户有行为的商品ID
        Set<Long> userProductIds = userBehaviorMapper.selectList(
                new LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .select(UserBehavior::getProductId)
        ).stream().map(UserBehavior::getProductId).collect(Collectors.toSet());
        System.out.println("User has behavior on " + userProductIds.size() + " products");

        // 1. 协同过滤推荐
        List<ProductScore> cfRecommendations = getCollaborativeFilteringRecommendations(userId, userProductIds, 20);
        System.out.println("CF recommendations count: " + cfRecommendations.size());

        // 2. 基于内容的推荐
        List<ProductScore> contentRecommendations = getContentBasedRecommendations(userId, userProductIds, 20);
        System.out.println("Content recommendations count: " + contentRecommendations.size());

        // 3. 热门推荐
        List<ProductScore> hotRecommendations = getHotRecommendations(userProductIds, 20);
        System.out.println("Hot recommendations count: " + hotRecommendations.size());

        // 合并推荐结果
        Map<Long, Double> productScores = new HashMap<>();

        // 合并协同过滤结果
        for (ProductScore ps : cfRecommendations) {
            productScores.put(ps.getProductId(), cfWeight * ps.getScore());
        }

        // 合并基于内容的结果
        for (ProductScore ps : contentRecommendations) {
            double currentScore = productScores.getOrDefault(ps.getProductId(), 0.0);
            productScores.put(ps.getProductId(), currentScore + contentWeight * ps.getScore());
        }

        // 合并热门推荐结果
        for (ProductScore ps : hotRecommendations) {
            double currentScore = productScores.getOrDefault(ps.getProductId(), 0.0);
            productScores.put(ps.getProductId(), currentScore + hotWeight * ps.getScore());
        }
        
        System.out.println("Total unique products after merge: " + productScores.size());
        if (productScores.size() <= 5) {
            System.out.println("Product scores: " + productScores);
        }

        // 按得分排序，取前limit个
        List<Long> recommendedProductIds = productScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        System.out.println("Final recommended product IDs: " + recommendedProductIds);

        // 查询商品详情
        if (recommendedProductIds.isEmpty()) {
            System.out.println("No recommendations, falling back to hot+new");
            return getHotAndNewProducts(limit);
        }

        List<Product> result = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .in(Product::getId, recommendedProductIds)
        );
        System.out.println("Returning " + result.size() + " products");
        return result;
    }

    @Override
    public List<Product> getHotAndNewProducts(int limit) {
        int hotLimit = limit / 2;
        int newLimit = limit - hotLimit;

        // 获取热门商品
        List<Product> hotProducts = getHotProducts(hotLimit);

        // 获取新品（按创建时间排序）
        List<Product> newProducts = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .orderByDesc(Product::getCreateTime)
                        .last("LIMIT " + newLimit)
        );

        // 合并去重
        Set<Long> productIds = new HashSet<>();
        List<Product> result = new ArrayList<>();

        // 添加热门商品
        for (Product product : hotProducts) {
            if (productIds.add(product.getId())) {
                result.add(product);
            }
        }

        // 添加新品
        for (Product product : newProducts) {
            if (productIds.add(product.getId())) {
                result.add(product);
            }
        }

        // 如果数量不足，用热门商品补齐
        if (result.size() < limit) {
            List<Product> moreHotProducts = productMapper.selectList(
                    new LambdaQueryWrapper<Product>()
                            .notIn(Product::getId, productIds)
                            .orderByDesc(Product::getSales)
                            .last("LIMIT " + (limit - result.size()))
            );
            result.addAll(moreHotProducts);
        }

        // 截取指定数量
        return result.subList(0, Math.min(result.size(), limit));
    }

    @Override
    public List<Product> getHotProducts(int limit) {
        return productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .orderByDesc(Product::getSales)
                        .last("LIMIT " + limit)
        );
    }

    /**
     * 协同过滤推荐
     */
    private List<ProductScore> getCollaborativeFilteringRecommendations(Long userId, Set<Long> userProductIds, int limit) {
        // 获取用户有行为的商品
        List<Product> userProducts = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .in(Product::getId, userProductIds)
        );

        System.out.println("CF: User has " + userProducts.size() + " products with behavior");

        if (userProducts.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取用户商品的IP和分类
        Set<Long> animeIpIds = userProducts.stream()
                .map(Product::getAnimeIpId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> categoryIds = userProducts.stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        System.out.println("CF: animeIpIds=" + animeIpIds + ", categoryIds=" + categoryIds);

        // 查询同IP或同分类的商品
        List<Product> candidateProducts = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .notIn(Product::getId, userProductIds)
                        .and(qw -> qw
                                .in(Product::getAnimeIpId, animeIpIds)
                                .or()
                                .in(Product::getCategoryId, categoryIds)
                        )
        );
        
        System.out.println("CF: Found " + candidateProducts.size() + " candidate products");

        if (candidateProducts.isEmpty()) {
            return Collections.emptyList();
        }

        // 计算销量和热度的归一化值
        int maxSales = candidateProducts.stream().mapToInt(Product::getSales).max().orElse(1);
        if (maxSales == 0) maxSales = 1;

        // 计算得分
        List<ProductScore> scores = new ArrayList<>();
        for (Product product : candidateProducts) {
            double salesScore = (double) product.getSales() / maxSales;
            double hotScore = 0.0;
            // 简单热度计算：销量占比
            hotScore = salesScore;
            double score = salesScore * 0.7 + hotScore * 0.3;
            scores.add(new ProductScore(product.getId(), score));
        }

        // 排序并返回前limit个
        return scores.stream()
                .sorted(Comparator.comparingDouble(ProductScore::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 基于内容的推荐
     */
    private List<ProductScore> getContentBasedRecommendations(Long userId, Set<Long> userProductIds, int limit) {
        // 获取用户有行为的商品
        List<Product> userProducts = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .in(Product::getId, userProductIds)
        );

        if (userProducts.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取用户商品的标签
        Set<String> userTags = new HashSet<>();
        for (Product product : userProducts) {
            if (product.getTags() != null) {
                String[] tags = product.getTags().split(",");
                userTags.addAll(Arrays.asList(tags));
            }
        }

        if (userTags.isEmpty()) {
            return Collections.emptyList();
        }

        // 查询所有商品
        List<Product> allProducts = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .notIn(Product::getId, userProductIds)
        );

        // 计算Jaccard相似度
        List<ProductScore> scores = new ArrayList<>();
        for (Product product : allProducts) {
            if (product.getTags() != null && !product.getTags().isEmpty()) {
                Set<String> productTags = new HashSet<>(Arrays.asList(product.getTags().split(",")));
                double similarity = calculateJaccardSimilarity(userTags, productTags);
                if (similarity > 0) {
                    scores.add(new ProductScore(product.getId(), similarity));
                }
            }
        }

        // 排序并返回前limit个
        return scores.stream()
                .sorted(Comparator.comparingDouble(ProductScore::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 热门推荐
     */
    private List<ProductScore> getHotRecommendations(Set<Long> userProductIds, int limit) {
        // 查询所有商品（不排除用户已有行为的商品，确保有足够的候选）
        List<Product> allProducts = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
        );

        if (allProducts.isEmpty()) {
            return Collections.emptyList();
        }

        // 计算销量归一化得分
        int maxSales = allProducts.stream().mapToInt(Product::getSales).max().orElse(1);
        if (maxSales == 0) maxSales = 1;

        List<ProductScore> scores = new ArrayList<>();
        for (Product product : allProducts) {
            double score = (double) product.getSales() / maxSales;
            scores.add(new ProductScore(product.getId(), score));
        }

        // 排序并返回前limit个
        return scores.stream()
                .sorted(Comparator.comparingDouble(ProductScore::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 计算Jaccard相似度
     */
    private double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }

        // 计算交集大小
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        // 计算并集大小
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    /**
     * 商品得分类
     */
    private static class ProductScore {
        private final Long productId;
        private final double score;

        public ProductScore(Long productId, double score) {
            this.productId = productId;
            this.score = score;
        }

        public Long getProductId() {
            return productId;
        }

        public double getScore() {
            return score;
        }
    }
}