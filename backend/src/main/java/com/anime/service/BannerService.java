package com.anime.service;

import com.anime.entity.Banner;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BannerService extends IService<Banner> {

    List<Banner> getActiveBanners();
}
