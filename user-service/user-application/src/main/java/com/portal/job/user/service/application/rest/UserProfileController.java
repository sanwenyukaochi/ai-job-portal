package com.portal.job.user.service.application.rest;

import com.portal.job.user.service.application.assembler.UserResponseAssembler;
import com.portal.job.user.service.application.dto.request.UpdateUserProfileRequest;
import com.portal.job.user.service.application.dto.response.UserResponse;
import com.portal.job.user.service.application.ports.input.service.UserManagementApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/profile")
public class UserProfileController {
    private final UserManagementApplicationService userManagementApplicationService;

    public UserProfileController(UserManagementApplicationService userManagementApplicationService) {
        this.userManagementApplicationService = userManagementApplicationService;
    }

    @GetMapping
    public ResponseEntity<UserResponse> getProfile(@RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(
                UserResponseAssembler.toResponse(userManagementApplicationService.getCurrentProfile(email)));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("X-User-Email") String email,
            @RequestBody @Valid UpdateUserProfileRequest request) {
        return ResponseEntity.ok(
                UserResponseAssembler.toResponse(
                        userManagementApplicationService.updateCurrentProfile(
                                email, request.fullName(), request.phone(), request.avatarUrl())));
    }
}
