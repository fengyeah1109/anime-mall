package com.anime.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 20, message = "username length must be between 3 and 20")
    private String username;

    @NotBlank(message = "identifier is required")
    private String identifier; // phone or email

    @NotBlank(message = "code is required")
    private String code;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "password too short")
    private String password;
    
    @NotBlank(message = "nickname is required")
    @Size(min = 2, max = 20, message = "nickname length must be between 2 and 20")
    private String nickname;
    
    private Integer gender;
    
    private LocalDate birthday;
}

