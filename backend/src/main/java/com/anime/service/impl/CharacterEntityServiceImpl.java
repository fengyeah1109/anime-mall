package com.anime.service.impl;

import com.anime.entity.CharacterEntity;
import com.anime.mapper.CharacterEntityMapper;
import com.anime.service.CharacterEntityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CharacterEntityServiceImpl extends ServiceImpl<CharacterEntityMapper, CharacterEntity> implements CharacterEntityService {
}

