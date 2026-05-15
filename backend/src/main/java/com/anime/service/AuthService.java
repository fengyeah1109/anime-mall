package com.anime.service;

import java.time.LocalDate;

public interface AuthService {

    String sendVerificationCode(String identifier);

    void register(String username, String identifier, String code, String password, String nickname, Integer gender, LocalDate birthday);

    String login(String identifier, String password);
}

