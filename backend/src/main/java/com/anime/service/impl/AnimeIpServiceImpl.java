package com.anime.service.impl;

import com.anime.entity.AnimeIp;
import com.anime.mapper.AnimeIpMapper;
import com.anime.service.AnimeIpService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AnimeIpServiceImpl extends ServiceImpl<AnimeIpMapper, AnimeIp> implements AnimeIpService {
}

