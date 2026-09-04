package com.web_site.JWT.service;

import com.web_site.JWT.model.BlacklistedToken;
import com.web_site.JWT.repository.BlacklistedTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    public void blacklistToken(String token) {
        if (blacklistedTokenRepository.existsByToken(token)) {
            return;
        }

        BlacklistedToken blacklistedToken = new BlacklistedToken();
        blacklistedToken.setToken(token);
        blacklistedToken.setBlacklistedAt(LocalDateTime.now());

        blacklistedTokenRepository.save(blacklistedToken);

        log.debug("Токен добавлен в чёрный список");
    }

    public boolean isBlackListed(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }
}
