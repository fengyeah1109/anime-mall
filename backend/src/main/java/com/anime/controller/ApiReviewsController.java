package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.Order;
import com.anime.entity.OrderItem;
import com.anime.entity.Review;
import com.anime.security.AuthContext;
import com.anime.service.OrderItemService;
import com.anime.service.OrderService;
import com.anime.service.ReviewService;
import com.anime.util.FileStorageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ApiReviewsController {

    private final ReviewService reviewService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final FileStorageService fileStorageService;

    @PostMapping
    public Result<Void> create(
            @RequestParam Long orderItemId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String content,
            @RequestPart(required = false) List<MultipartFile> images
    ) {
        Long userId = AuthContext.getUserId();
        OrderItem orderItem = orderItemService.getById(orderItemId);
        if (orderItem == null) {
            return Result.fail("order item not found");
        }
        Order order = orderService.getById(orderItem.getOrderId());
        if (order == null || !order.getUserId().equals(userId) || order.getStatus() != 4) {
            return Result.fail("order not completed");
        }

        Review exist = reviewService.getOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderItemId, orderItemId)
                .eq(Review::getUserId, userId));
        if (exist != null) {
            return Result.fail("already reviewed");
        }

        String imageStr = null;
        if (images != null && !images.isEmpty()) {
            imageStr = images.stream().map(f -> fileStorageService.store(f, "reviews")).collect(Collectors.joining(","));
        }

        Review review = new Review();
        review.setOrderItemId(orderItemId);
        review.setUserId(userId);
        review.setProductId(orderItem.getProductId());
        review.setRating(rating);
        review.setContent(content);
        review.setImages(imageStr);
        reviewService.save(review);
        return Result.success();
    }

    @GetMapping
    public Result<List<Review>> list() {
        Long userId = AuthContext.getUserId();
        List<Review> reviews = reviewService.list(new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreateTime));
        
        // 关联商品信息
        reviews.forEach(review -> {
            OrderItem orderItem = orderItemService.getById(review.getOrderItemId());
            if (orderItem != null) {
                review.setProductName(orderItem.getProductName());
                review.setProductImage(orderItem.getProductImage());
                review.setPrice(orderItem.getPrice());
            }
        });
        
        return Result.success(reviews);
    }

    @PutMapping("/{id}")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestParam Integer rating,
            @RequestParam(required = false) String content,
            @RequestPart(required = false) List<MultipartFile> images
    ) {
        Long userId = AuthContext.getUserId();
        Review review = reviewService.getById(id);
        if (review == null || !review.getUserId().equals(userId)) {
            return Result.fail("review not found");
        }

        String imageStr = null;
        if (images != null && !images.isEmpty()) {
            imageStr = images.stream().map(f -> fileStorageService.store(f, "reviews")).collect(Collectors.joining(","));
        }

        review.setRating(rating);
        review.setContent(content);
        if (imageStr != null) {
            review.setImages(imageStr);
        }
        reviewService.updateById(review);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = AuthContext.getUserId();
        Review review = reviewService.getById(id);
        if (review == null || !review.getUserId().equals(userId)) {
            return Result.fail("review not found");
        }
        reviewService.removeById(id);
        return Result.success();
    }
}

