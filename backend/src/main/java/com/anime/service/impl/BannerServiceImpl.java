package com.anime.service.impl;

import com.anime.entity.Banner;
import com.anime.mapper.BannerMapper;
import com.anime.service.BannerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public List<Banner> getActiveBanners() {
        return baseMapper.selectList(
                new LambdaQueryWrapper<Banner>()
                        .eq(Banner::getStatus, 1)
                        .orderByAsc(Banner::getSortOrder)
        );
    }
}
