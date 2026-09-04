package com.web_site.JWT.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh токен обязателен")
    private String refreshToken;
}
