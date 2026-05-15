package com.anime.service.impl;

import com.anime.entity.Favorite;
import com.anime.mapper.FavoriteMapper;
import com.anime.service.FavoriteService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {
}

