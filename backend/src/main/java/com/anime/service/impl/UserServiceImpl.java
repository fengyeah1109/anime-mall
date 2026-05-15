package com.anime.service.impl;

import com.anime.entity.User;
import com.anime.mapper.UserMapper;
import com.anime.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}

