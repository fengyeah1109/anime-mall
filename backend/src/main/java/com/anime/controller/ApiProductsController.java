package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.*;
import com.anime.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.anime.vo.ProductDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ApiProductsController {

    private final ProductService productService;
    private final ProductImageService productImageService;
    private final AnimeIpService animeIpService;
    private final CharacterEntityService characterEntityService;
    private final ReviewService reviewService;

    @GetMapping
    public Result<Page<Product>> list(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "animeIpId", required = false) Long animeIpId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "sortBy", defaultValue = "createTime") String sortBy,
            @RequestParam(value = "order", defaultValue = "desc") String order,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1);

        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (animeIpId != null) {
            wrapper.eq(Product::getAnimeIpId, animeIpId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or().like(Product::getTags, keyword)
                    .or().like(Product::getDescription, keyword));
        }
        if (minPrice != null) {
            wrapper.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(Product::getPrice, maxPrice);
        }

        boolean asc = "asc".equalsIgnoreCase(order);
        if ("price".equalsIgnoreCase(sortBy)) {
            if (asc) wrapper.orderByAsc(Product::getPrice);
            else wrapper.orderByDesc(Product::getPrice);
        } else if ("sales".equalsIgnoreCase(sortBy)) {
            if (asc) wrapper.orderByAsc(Product::getSales);
            else wrapper.orderByDesc(Product::getSales);
        } else {
            if (asc) wrapper.orderByAsc(Product::getCreateTime);
            else wrapper.orderByDesc(Product::getCreateTime);
        }

        return Result.success(productService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/anime-ips")
    public Result<List<AnimeIp>> listAnimeIps() {
        return Result.success(animeIpService.list(new LambdaQueryWrapper<AnimeIp>().eq(AnimeIp::getStatus, 1)));
    }

    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable("id") Long id) {
        Product product = productService.getOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id)
                .eq(Product::getStatus, 1)
        );
        if (product == null) {
            return Result.fail("product not found");
        }

        List<ProductImage> images = productImageService.list(
                new LambdaQueryWrapper<ProductImage>().eq(ProductImage::getProductId, id).orderByAsc(ProductImage::getSortOrder)
        );

        AnimeIp animeIp = product.getAnimeIpId() == null ? null :
                animeIpService.getById(product.getAnimeIpId());

        List<CharacterEntity> characters = parseCharacterIds(product.getCharacterIds()).isEmpty()
                ? Collections.emptyList()
                : characterEntityService.list(
                new LambdaQueryWrapper<CharacterEntity>().in(CharacterEntity::getId, parseCharacterIds(product.getCharacterIds()))
        );

        List<Review> latestReviews = reviewService.list(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getProductId, id)
                        .orderByDesc(Review::getCreateTime)
                        .last("limit 5")
        );

        ProductDetailVO vo = new ProductDetailVO();
        vo.setProduct(product);
        vo.setImages(images);
        vo.setAnimeIp(animeIp);
        vo.setCharacters(characters);
        vo.setLatestReviews(latestReviews);

        return Result.success(vo);
    }

    private List<Long> parseCharacterIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}

