package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.Address;
import com.anime.security.AuthContext;
import com.anime.service.AddressService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class ApiAddressesController {

    private final AddressService addressService;

    @GetMapping
    public Result<List<Address>> list() {
        Long userId = AuthContext.getUserId();
        return Result.success(addressService.list(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId)));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Address address) {
        Long userId = AuthContext.getUserId();
        address.setUserId(userId);
        addressService.save(address);
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            setDefaultInternal(userId, address.getId());
        }
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Address address) {
        Long userId = AuthContext.getUserId();
        address.setId(id);
        address.setUserId(userId);
        addressService.updateById(address);
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            setDefaultInternal(userId, id);
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = AuthContext.getUserId();
        // 防止跨用户删除
        addressService.remove(new LambdaQueryWrapper<Address>().eq(Address::getId, id).eq(Address::getUserId, userId));
        return Result.success();
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        Long userId = AuthContext.getUserId();
        setDefaultInternal(userId, id);
        return Result.success();
    }

    private void setDefaultInternal(Long userId, Long addressId) {
        // 1) 清空
        addressService.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0)
        );
        // 2) 设置
        addressService.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getId, addressId)
                .set(Address::getIsDefault, 1)
        );
    }
}

