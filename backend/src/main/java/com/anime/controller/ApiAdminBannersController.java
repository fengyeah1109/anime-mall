package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.Banner;
import com.anime.service.BannerService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class ApiAdminBannersController {

    private final BannerService bannerService;

    @GetMapping
    public Result<Page<Banner>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Page<Banner> bannerPage = bannerService.page(new Page<>(page, size));
        return Result.success(bannerPage);
    }

    @PostMapping
    public Result<Void> create(@RequestBody Banner banner) {
        bannerService.save(banner);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Banner banner) {
        banner.setId(id);
        bannerService.updateById(banner);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bannerService.removeById(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Banner> get(@PathVariable Long id) {
        Banner banner = bannerService.getById(id);
        return Result.success(banner);
    }
}
