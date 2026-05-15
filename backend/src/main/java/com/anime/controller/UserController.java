package com.anime.controller;

import com.anime.common.Result;
import com.anime.entity.User;
import com.anime.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<List<User>> list() {
        return Result.success(userService.list());
    }

    @GetMapping("/{id}")
    public Result<User> get(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody User user) {
        userService.save(user);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }
}

