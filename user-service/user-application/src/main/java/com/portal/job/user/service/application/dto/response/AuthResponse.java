package com.portal.job.user.service.application.dto.response;

public record AuthResponse(String title, String message, String token, UserResponse user) {}
