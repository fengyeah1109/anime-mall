package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.Admin;
import com.anime.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public Result<List<Admin>> list() {
        return Result.success(adminService.list());
    }

    @GetMapping("/{id}")
    public Result<Admin> get(@PathVariable Long id) {
        return Result.success(adminService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Admin admin) {
        adminService.save(admin);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Admin admin) {
        adminService.updateById(admin);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminService.removeById(id);
        return Result.success();
    }
}

