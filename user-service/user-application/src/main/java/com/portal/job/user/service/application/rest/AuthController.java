package com.portal.job.user.service.application.rest;

import com.portal.job.user.service.application.assembler.UserResponseAssembler;
import com.portal.job.user.service.application.dto.request.LoginRequest;
import com.portal.job.user.service.application.dto.request.SignupRequest;
import com.portal.job.user.service.application.dto.response.AuthResponse;
import com.portal.job.user.service.application.ports.input.service.AuthApplicationService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody @Valid SignupRequest request) {
        return ResponseEntity.ok(
                UserResponseAssembler.toResponse(
                        authApplicationService.registerLocal(
                                request.fullName(),
                                request.email(),
                                request.password(),
                                request.phone(),
                                request.role())));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(
                UserResponseAssembler.toResponse(
                        authApplicationService.login(request.email(), request.password())));
    }
}
