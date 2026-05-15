package com.anime.service.impl;

import com.anime.entity.Admin;
import com.anime.mapper.AdminMapper;
import com.anime.service.AdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
}

