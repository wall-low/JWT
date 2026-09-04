package com.web_site.JWT.controller;

import com.web_site.JWT.dto.LoginRequest;
import com.web_site.JWT.dto.LoginResponse;
import com.web_site.JWT.dto.RefreshTokenRequest;
import com.web_site.JWT.dto.RegisterRequest;
import com.web_site.JWT.exception.InvalidTokenException;
import com.web_site.JWT.exception.UserAlreadyExistsException;
import com.web_site.JWT.model.User;
import com.web_site.JWT.repository.UserRepository;
import com.web_site.JWT.security.JwtTokenProvider;
import com.web_site.JWT.service.TokenBlacklistService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

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
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String accessToken = jwtTokenProvider.generateAccessToken(request.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.getUsername());

        log.info("Пользователь {} вошёл в систему", request.getUsername());

        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, request.getUsername()));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException(request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");

        userRepository.save(user);

        log.info("Зарегистрирован пользователь {}", request.getUsername());

        return ResponseEntity.ok(Map.of("message", "Пользователь успешно зарегистрирован"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("Refresh токен недействителен или истёк");
        }

        if (tokenBlacklistService.isBlackListed(refreshToken)) {
            throw new InvalidTokenException("Refresh токен отозван, войдите заново");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        String newAccessToken = jwtTokenProvider.generateAccessToken(username);

        log.debug("Обновлён access токен для пользователя {}", username);

        return ResponseEntity.ok(new LoginResponse(newAccessToken, refreshToken, username));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Заголовок Authorization отсутствует или задан неверно");
        }

        String token = authHeader.substring(7);
        tokenBlacklistService.blacklistToken(token);

        return ResponseEntity.ok(Map.of("message", "Вы успешно вышли из системы"));
    }
}
