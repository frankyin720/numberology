package com.webest.numerology.web;

import com.webest.numerology.security.UserPrincipal;
import com.webest.numerology.service.UserProfileService;
import com.webest.numerology.web.dto.ProfileStatusResponse;
import com.webest.numerology.web.dto.UserProfileDto;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserProfileService profileService;

    public ProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileStatusResponse status(@AuthenticationPrincipal UserPrincipal principal) {
        return profileService.getStatus(principal.getUserId());
    }

    @PutMapping
    public UserProfileDto save(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody UserProfileDto body) {
        return profileService.save(principal.getUserId(), body);
    }
}
