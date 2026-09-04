package com.web_site.JWT.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Test
    @DisplayName("Из выданного токена достаётся то же имя пользователя")
    void generatedTokenContainsUsername() {
        String username = "danila";

        String token = tokenProvider.generateAccessToken(username);

        assertThat(tokenProvider.getUsernameFromToken(token)).isEqualTo(username);
    }

    @Test
    @DisplayName("Свежевыданный токен проходит проверку")
    void freshTokenIsValid() {
        String token = tokenProvider.generateAccessToken("danila");

        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Произвольная строка не принимается за токен")
    void garbageStringIsNotValid() {
        assertThat(tokenProvider.validateToken("это-точно-не-токен")).isFalse();
    }

    @Test
    @DisplayName("Access и refresh токены для одного пользователя различаются")
    void accessAndRefreshTokensAreDifferent() {
        String username = "danila";

        String accessToken = tokenProvider.generateAccessToken(username);
        String refreshToken = tokenProvider.generateRefreshToken(username);

        assertThat(accessToken).isNotEqualTo(refreshToken);
    }

    @Test
    @DisplayName("Токен, подписанный чужим ключом, не принимается")
    void tokenSignedWithForeignKeyIsRejected() {
        SecretKey foreignKey = Keys.hmacShaKeyFor(
                "chuzhoy-klyuch-dlinoy-ne-menee-32-baytov-dlya-HS256".getBytes());

        String foreignToken = Jwts.builder()
                .subject("hacker")
                .signWith(foreignKey)
                .compact();

        assertThat(tokenProvider.validateToken(foreignToken)).isFalse();
    }

    @Test
    @DisplayName("Два токена одного пользователя, выпущенные подряд, различаются")
    void tokensIssuedInSameSecondAreDifferent() {
        String first = tokenProvider.generateAccessToken("danila");
        String second = tokenProvider.generateAccessToken("danila");

        assertThat(first).isNotEqualTo(second);
    }
}
