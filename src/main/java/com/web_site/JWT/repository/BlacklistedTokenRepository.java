package com.web_site.JWT.repository;

import com.web_site.JWT.model.BlacklistedToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    boolean existsByToken(String token);

    @Transactional
    void deleteByBlacklistedAtBefore(LocalDateTime data);

}
