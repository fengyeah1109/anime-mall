package com.anime.service.impl;

import com.anime.entity.Address;
import com.anime.mapper.AddressMapper;
import com.anime.service.AddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {
}

