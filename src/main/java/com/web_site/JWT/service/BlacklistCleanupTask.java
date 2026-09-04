package com.web_site.JWT.service;

import com.web_site.JWT.repository.BlacklistedTokenRepository;
import com.web_site.JWT.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Убирает из чёрного списка токены, срок жизни которых уже истёк.
 *
 * Отозванный токен нужно помнить ровно до того момента, когда он протухнет сам.
 * После этого запись бесполезна: такой токен не пройдёт проверку подписи по сроку
 * в любом случае. Без чистки таблица растёт бесконечно, а по ней идёт поиск
 * при каждом обновлении сессии.
 */
@Component
public class BlacklistCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(BlacklistCleanupTask.class);

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Scheduled(fixedDelayString = "${jwt.blacklist-cleanup-interval}")
    public void removeExpiredTokens() {
        LocalDateTime threshold = LocalDateTime.now()
                .minusNanos(jwtTokenProvider.getRefreshTokenExpiration() * 1_000_000);

        long removed = blacklistedTokenRepository.deleteByBlacklistedAtBefore(threshold);

        if (removed > 0) {
            log.info("Из чёрного списка удалено просроченных токенов: {}", removed);
        }
    }
}
