package com.web_site.JWT.controller;

import com.web_site.JWT.dto.LoginRequest;
import com.web_site.JWT.dto.LoginResponse;
import com.web_site.JWT.dto.RefreshTokenRequest;
import com.web_site.JWT.model.User;
import com.web_site.JWT.repository.UserRepository;
import com.web_site.JWT.security.JwtTokenProvider;
import com.web_site.JWT.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            String accessToken = jwtTokenProvider.generateAccessToken(loginRequest.getUsername());
            String refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.getUsername());

            return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, loginRequest.getUsername()));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Неверный логин или пароль");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();

            if (jwtTokenProvider.validateToken(refreshToken)
                    && !tokenBlacklistService.isBlackListed(refreshToken)) {

                String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

                String newAccessToken = jwtTokenProvider.generateAccessToken(username);

                return ResponseEntity.ok(new LoginResponse(newAccessToken, refreshToken, username));
            }

            return ResponseEntity.status(401).body("Невалидный refresh токен");

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Ошибка обновления токена");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);

            tokenBlacklistService.blacklistToken(token);

            return ResponseEntity.ok("Вы успешно вышли из системы");

        } catch (Exception e) {
            return ResponseEntity.status(400).body("Ошибка при выходе");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest registerRequest) {
        try {
            if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
                return ResponseEntity.status(400).body("Пользователь уже существует");
            }


            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole("ROLE_USER");

            userRepository.save(user);

            return ResponseEntity.ok("Пользователь успешно зарегистрирован");

        } catch (Exception e) {
            return ResponseEntity.status(400).body("Ошибка регистрации");
        }
    }


}
