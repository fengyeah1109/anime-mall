package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.ProductCategory;
import com.anime.service.ProductCategoryService;
import com.anime.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class ApiCategoriesController {

    private final ProductCategoryService productCategoryService;

    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        // 只返回分类列表（一级分类）
        List<ProductCategory> categories = productCategoryService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getLevel, 1)
                        .eq(ProductCategory::getStatus, 1)
                        .orderByAsc(ProductCategory::getSortOrder)
        );
        List<CategoryVO> vo = categories.stream().map(c -> {
            CategoryVO x = new CategoryVO();
            x.setId(c.getId());
            x.setParentId(c.getParentId());
            x.setName(c.getName());
            x.setLevel(c.getLevel());
            x.setSortOrder(c.getSortOrder());
            x.setIcon(c.getIcon());
            x.setStatus(c.getStatus());
            return x;
        }).toList();
        return Result.success(vo);
    }
}

