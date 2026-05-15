package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.Banner;
import com.anime.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class ApiBannersController {

    private final BannerService bannerService;

    @GetMapping
    public Result<List<Banner>> getActiveBanners() {
        List<Banner> banners = bannerService.getActiveBanners();
        return Result.success(banners);
    }
}
