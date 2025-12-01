package com.web_site.JWT.service;

import com.web_site.JWT.model.BlacklistedToken;
import com.web_site.JWT.repository.BlacklistedTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenBlacklistService {

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    public void blacklistToken(String token) {
        BlacklistedToken blacklistedToken = new BlacklistedToken();
        blacklistedToken.setToken(token);
        blacklistedToken.setBlacklistedAt(LocalDateTime.now());

        blacklistedTokenRepository.save(blacklistedToken);

        System.out.println("Токен добавлен в черный список");
    }

    public boolean isBlackListed(String token){
        return blacklistedTokenRepository.existsByToken(token);
    }

}
