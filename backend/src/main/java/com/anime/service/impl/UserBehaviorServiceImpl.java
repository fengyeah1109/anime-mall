package com.anime.service.impl;

import com.anime.entity.UserBehavior;
import com.anime.mapper.UserBehaviorMapper;
import com.anime.service.UserBehaviorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserBehaviorServiceImpl extends ServiceImpl<UserBehaviorMapper, UserBehavior> implements UserBehaviorService {
}

