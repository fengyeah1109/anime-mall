package com.anime.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdateRequest {

    private String phone;

    @Email(message = "email format invalid")
    private String email;

    @Size(max = 50, message = "nickname too long")
    private String nickname;

    private Integer gender; // 0/1/2

    private LocalDate birthday;
}

