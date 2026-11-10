package com.webest.numerology.repository;

import com.webest.numerology.repository.entity.AuthTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthTokenEntity, String> {

    @Query("SELECT t FROM AuthTokenEntity t JOIN FETCH t.user WHERE t.token = :token AND t.expiresAt > :now")
    Optional<AuthTokenEntity> findByTokenAndExpiresAtAfter(@Param("token") String token, @Param("now") Instant now);
}
