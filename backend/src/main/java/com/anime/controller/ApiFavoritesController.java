package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.Favorite;
import com.anime.entity.Product;
import com.anime.security.AuthContext;
import com.anime.service.FavoriteService;
import com.anime.service.ProductService;
import com.anime.vo.FavoriteVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class ApiFavoritesController {

    private final FavoriteService favoriteService;
    private final ProductService productService;

    @PostMapping
    public Result<Void> add(@RequestParam Long productId) {
        Long userId = AuthContext.getUserId();
        Favorite exist = favoriteService.getOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        if (exist == null) {
            Favorite f = new Favorite();
            f.setUserId(userId);
            f.setProductId(productId);
            favoriteService.save(f);
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        favoriteService.remove(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getId, id)
                .eq(Favorite::getUserId, AuthContext.getUserId()));
        return Result.success();
    }

    @GetMapping
    public Result<List<FavoriteVO>> list() {
        List<Favorite> favorites = favoriteService.list(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, AuthContext.getUserId())
                .orderByDesc(Favorite::getCreateTime));
        
        List<FavoriteVO> voList = favorites.stream().map(f -> {
            FavoriteVO vo = new FavoriteVO();
            vo.setId(f.getId());
            vo.setProductId(f.getProductId());
            vo.setCreateTime(f.getCreateTime());
            
            Product product = productService.getById(f.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setProductImage(product.getMainImage());
                vo.setPrice(product.getPrice());
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        return Result.success(voList);
    }
}

