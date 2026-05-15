package com.anime.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendCodeRequest {

    @NotBlank(message = "identifier is required")
    private String identifier;
}

