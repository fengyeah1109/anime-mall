package com.anime.service.impl;

import com.anime.entity.Admin;
import com.anime.entity.User;
import com.anime.exception.BusinessException;
import com.anime.mapper.AdminMapper;
import com.anime.mapper.UserMapper;
import com.anime.security.JwtUtil;
import com.anime.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final AdminMapper adminMapper;
    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();

    private record CodeEntry(String code, Instant expireAt) {
    }

    @Override
    public String sendVerificationCode(String identifier) {
        // 开发环境固定验证码为123456
        String code = "123456";
        codeStore.put(identifier, new CodeEntry(code, Instant.now().plus(CODE_TTL)));
        return code;
    }

    @Override
    public void register(String username, String identifier, String code, String password, String nickname, Integer gender, LocalDate birthday) {
        if (username == null || username.isBlank()) {
            throw new BusinessException("username is required");
        }
        if (identifier == null || identifier.isBlank()) {
            throw new BusinessException("identifier is required");
        }
        if (code == null || code.isBlank()) {
            throw new BusinessException("code is required");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException("password too short");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException("nickname is required");
        }

        CodeEntry entry = codeStore.get(identifier);
        if (entry == null || entry.expireAt().isBefore(Instant.now()) || !entry.code().equals(code)) {
            throw new BusinessException(400, "invalid verification code");
        }

        // 检查用户名、手机号或邮箱是否已存在
        User exist = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username).or()
                .eq(User::getPhone, identifier).or()
                .eq(User::getEmail, identifier));
        if (exist != null) {
            throw new BusinessException(409, "user already exists");
        }

        User user = new User();
        user.setUsername(username);
        if (identifier.contains("@")) {
            user.setEmail(identifier);
        } else {
            user.setPhone(identifier);
        }
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setGender(gender);
        user.setBirthday(birthday);
        user.setStatus(1);
        userMapper.insert(user);
    }

    @Override
    public String login(String identifier, String password) {
        if (identifier == null || identifier.isBlank() || password == null) {
            throw new BusinessException("identifier and password are required");
        }

        // 先查用户
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, identifier)
                .or().eq(User::getPhone, identifier)
                .or().eq(User::getEmail, identifier));
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return jwtUtil.generateToken(user.getId(), "USER");
        }

        // 再查管理员（只支持 username 登录）
        Admin admin = adminMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, identifier));
        if (admin != null && passwordEncoder.matches(password, admin.getPassword())) {
            return jwtUtil.generateToken(admin.getId(), "ADMIN");
        }

        throw new BusinessException(401, "invalid credentials");
    }
}

