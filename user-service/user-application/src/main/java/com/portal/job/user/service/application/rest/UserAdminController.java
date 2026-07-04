package com.portal.job.user.service.application.rest;

import com.portal.job.user.service.application.assembler.UserResponseAssembler;
import com.portal.job.user.service.application.dto.response.UserResponse;
import com.portal.job.user.service.application.ports.input.service.UserManagementApplicationService;
import com.portal.job.user.service.domain.core.valueobject.UserRole;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserAdminController {
    private final UserManagementApplicationService userManagementApplicationService;

    public UserAdminController(UserManagementApplicationService userManagementApplicationService) {
        this.userManagementApplicationService = userManagementApplicationService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(
                UserResponseAssembler.toResponse(userManagementApplicationService.getUserById(userId)));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(
                userManagementApplicationService.getAllUsers().stream()
                        .map(UserResponseAssembler::toResponse)
                        .toList());
    }

    @PatchMapping("/{userId}/suspend")
    public ResponseEntity<UserResponse> suspendUser(@PathVariable String userId) {
        return ResponseEntity.ok(
                UserResponseAssembler.toResponse(userManagementApplicationService.suspendUser(userId)));
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable String userId) {
        return ResponseEntity.ok(
                UserResponseAssembler.toResponse(userManagementApplicationService.activateUser(userId)));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable String userId) {
        return ResponseEntity.ok(
                UserResponseAssembler.toResponse(userManagementApplicationService.deleteUser(userId)));
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserResponse> changeUserRole(
            @PathVariable String userId, @RequestParam UserRole role) {
        return ResponseEntity.ok(
                UserResponseAssembler.toResponse(
                        userManagementApplicationService.changeUserRole(userId, role)));
    }
}
