package com.webest.numerology.web;

import com.webest.numerology.security.UserPrincipal;
import com.webest.numerology.service.AuthService;
import com.webest.numerology.web.dto.AuthResponse;
import com.webest.numerology.web.dto.ChangePasswordRequest;
import com.webest.numerology.web.dto.LoginRequest;
import com.webest.numerology.web.dto.RegisterRequest;
import com.webest.numerology.web.dto.ResendVerificationRequest;
import com.webest.numerology.web.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Map.of("message", "註冊成功，請至信箱點擊驗證連結後再登入");
    }

    @GetMapping("/verify")
    public Map<String, String> verify(@RequestParam String token) {
        authService.verifyEmail(token);
        return Map.of("message", "信箱驗證成功，請登入");
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/resend-verification")
    public Map<String, String> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerificationEmail(request.getEmail());
        return Map.of("message", "若該信箱有待驗證的帳號，我們已重新發送驗證信");
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal UserPrincipal principal) {
        return authService.getUser(principal.getUserId());
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(principal.getUserId(), request);
        return Map.of("message", "密碼已更新");
    }
}
