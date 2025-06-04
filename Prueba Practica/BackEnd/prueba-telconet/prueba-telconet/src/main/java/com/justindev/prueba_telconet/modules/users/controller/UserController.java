package com.justindev.prueba_telconet.modules.users.controller;

import com.justindev.prueba_telconet.modules.users.dto.*;
import com.justindev.prueba_telconet.modules.users.model.AppUser;
import com.justindev.prueba_telconet.modules.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update")
    public ResponseEntity<String> updateUser(
            @RequestBody UpdateUserDto data,
            @AuthenticationPrincipal AppUser user
    ) {
        service.updateUser(data, user.getId());
        return ResponseEntity.ok("User updated successfully");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/send-change-password-email")
    public ResponseEntity<String> sendChangePasswordEmail(
            @AuthenticationPrincipal AppUser user
    ) {
        service.sendCodeChangePassword(user.getEmail());
        return ResponseEntity.ok("Code sent successfully to " + user.getEmail() + ", check your email");
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestParam ChangePasswordDto passwordsDto,
            @RequestParam String code,
            @AuthenticationPrincipal AppUser user
    ) {
        service.changePassword(passwordsDto, code, user.getEmail());
        return ResponseEntity.ok("Password changed successfully");
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/verify-code")
    public ResponseEntity<Boolean> verifyCode(
            @RequestBody VerifyCodeDto data
    ) {
        return service.verifyCode(data.getCode(), data.getEmail())
                ? ResponseEntity.ok(true)
                : ResponseEntity.ok(false);
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestParam String email
    ) {
        service.sendCodeChangePassword(email);
        return ResponseEntity.ok("Code sent successfully to " + email + ", check your email");
    }

    @PreAuthorize("permitAll()")
    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam ChangePasswordDto passwordsDto,
            @RequestParam String code,
            @RequestParam String email
    ) {
        service.changePassword(passwordsDto, code, email);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new-admin")
    public ResponseEntity<String> createNewAdmin(
            @RequestBody UserRegisterDto data
    ) {
        service.registerAdmin(data);
        return ResponseEntity.ok("New admin created successfully");


    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/get-my-sessions")
    public ResponseEntity<List<MySessionsDto>> getMySessions(
            @AuthenticationPrincipal AppUser user

    ){
        return ResponseEntity.ok(service.getMySessions(user.getId()));
    }
}
