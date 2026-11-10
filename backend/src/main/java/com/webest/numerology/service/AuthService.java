package com.webest.numerology.service;

import com.webest.numerology.repository.AuthTokenRepository;
import com.webest.numerology.repository.UserRepository;
import com.webest.numerology.repository.entity.AuthTokenEntity;
import com.webest.numerology.repository.entity.UserEntity;
import com.webest.numerology.security.UserPrincipal;
import com.webest.numerology.web.dto.AuthResponse;
import com.webest.numerology.web.dto.ChangePasswordRequest;
import com.webest.numerology.web.dto.LoginRequest;
import com.webest.numerology.web.dto.RegisterRequest;
import com.webest.numerology.web.dto.UserDto;
import com.webest.numerology.web.exception.ApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${numerology.auth.verification-expiry-hours:24}")
    private int verificationExpiryHours;

    @Value("${numerology.auth.token-expiry-days:30}")
    private int tokenExpiryDays;

    public AuthService(
            UserRepository userRepository,
            AuthTokenRepository authTokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "此信箱已註冊");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        UserEntity user = UserEntity.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .verificationToken(token)
                .verificationExpiresAt(Instant.now().plusSeconds(verificationExpiryHours * 3600L))
                .createdAt(Instant.now())
                .build();
        userRepository.save(user);
        emailService.sendVerificationEmail(email, token);
    }

    @Transactional
    public void verifyEmail(String token) {
        if (StringUtils.isBlank(token)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "驗證連結無效");
        }
        UserEntity user = userRepository.findByVerificationToken(token.trim())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "驗證連結無效或已使用"));
        if (user.getVerificationExpiresAt() != null
                && user.getVerificationExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "驗證連結已過期，請重新發送驗證信");
        }
        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpiresAt(null);
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "信箱或密碼錯誤"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "信箱或密碼錯誤");
        }
        if (!user.isEmailVerified()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "請先至信箱完成驗證後再登入");
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        AuthTokenEntity authToken = AuthTokenEntity.builder()
                .token(token)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(tokenExpiryDays * 86400L))
                .createdAt(Instant.now())
                .build();
        authTokenRepository.save(authToken);
        return AuthResponse.builder()
                .token(token)
                .user(toDto(user))
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<UserPrincipal> resolveUser(String token) {
        if (StringUtils.isBlank(token)) {
            return Optional.empty();
        }
        return authTokenRepository.findByTokenAndExpiresAtAfter(token.trim(), Instant.now())
                .map(AuthTokenEntity::getUser)
                .map(user -> new UserPrincipal(user.getId(), user.getEmail()));
    }

    /**
     * 未驗證帳號重新發送驗證信；已驗證或不存在的信箱不回洩詳情。
     */
    @Transactional
    public void resendVerificationEmail(String email) {
        String normalized = normalizeEmail(email);
        userRepository.findByEmailIgnoreCase(normalized).ifPresent(user -> {
            if (user.isEmailVerified()) {
                return;
            }
            String token = UUID.randomUUID().toString().replace("-", "");
            user.setVerificationToken(token);
            user.setVerificationExpiresAt(Instant.now().plusSeconds(verificationExpiryHours * 3600L));
            userRepository.save(user);
            emailService.sendVerificationEmail(normalized, token);
        });
    }

    @Transactional(readOnly = true)
    public UserDto getUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "使用者不存在"));
        return toDto(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "使用者不存在"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "目前密碼不正確");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "新密碼不可與目前密碼相同");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private static String normalizeEmail(String email) {
        if (StringUtils.isBlank(email)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "請輸入信箱");
        }
        return email.trim().toLowerCase();
    }

    private static UserDto toDto(UserEntity user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
