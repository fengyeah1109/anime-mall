package com.anime.controller;

import com.anime.common.Result;
import com.anime.dto.UserProfileUpdateRequest;
import com.anime.entity.User;
import com.anime.security.AuthContext;
import com.anime.service.UserService;
import com.anime.util.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class ApiUserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping("/profile")
    public Result<User> profile() {
        Long userId = AuthContext.getUserId();
        return Result.success(userService.getById(userId));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        Long userId = AuthContext.getUserId();
        User user = userService.getById(userId);
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setGender(request.getGender());
        user.setBirthday(request.getBirthday());
        userService.updateById(user);
        return Result.success();
    }

    @PostMapping("/avatar")
    public Result<Void> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = AuthContext.getUserId();
        String url = fileStorageService.store(file, "avatars");
        User user = userService.getById(userId);
        user.setAvatar(url);
        userService.updateById(user);
        return Result.success();
    }
}

