package com.anime.controller;

import com.anime.common.Result;
import com.anime.dto.LoginRequest;
import com.anime.dto.LoginResponse;
import com.anime.dto.RegisterRequest;
import com.anime.dto.SendCodeRequest;
import com.anime.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final AuthService authService;

    // 可选但能让 register 流程闭环（公开接口 /api/auth/**）
    @PostMapping("/send-code")
    public Result<String> sendCode(@Valid @RequestBody SendCodeRequest request) {
        // 这里返回真实 code 仅用于开发联调；生产环境请改为短信/邮件发送
        String code = authService.sendVerificationCode(request.getIdentifier());
        return Result.success(code);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(
            request.getUsername(),
            request.getIdentifier(), 
            request.getCode(), 
            request.getPassword(),
            request.getNickname(),
            request.getGender(),
            request.getBirthday()
        );
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getIdentifier(), request.getPassword());
        return Result.success(new LoginResponse(token));
    }
}

