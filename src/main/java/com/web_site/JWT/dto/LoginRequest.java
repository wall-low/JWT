package com.web_site.JWT.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
