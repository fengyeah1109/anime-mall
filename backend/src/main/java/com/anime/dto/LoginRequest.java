package com.anime.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "identifier is required")
    private String identifier; // username / phone / email

    @NotBlank(message = "password is required")
    private String password;
}

