package com.justindev.prueba_telconet.modules.users.controller;

import com.justindev.prueba_telconet.modules.users.dto.*;
import com.justindev.prueba_telconet.modules.users.model.AppUser;
import com.justindev.prueba_telconet.modules.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for controlling user operations")
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
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    @Operation(
            summary = "Register",
            description = "Register a new user",
            tags = {"Users"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User data to register",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserRegisterDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User created successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserRegisterDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Error in the dto fields, non-compliance with validations",
                            content = @Content(mediaType = "application/json")

                    )

            }
    )
    public ResponseEntity<String> register(
            @RequestBody @Valid UserRegisterDto data
    ) {
        service.registerUser(data);
        return ResponseEntity.ok("User created successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/reset-password-by-admin")
    public ResponseEntity<String> resetPasswordByAdmin(
            @RequestParam Long userId
    ) {
        service.resetPassword(userId);
        return ResponseEntity.ok("Password reset successfully for user with ID: " + userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update-user-by-admin")
    public ResponseEntity<String> updateUserByAdmin(
            @RequestBody UpdateUserDto data,
            @RequestParam Long userId
    ) {
        service.updateUser(data, userId);
        return ResponseEntity.ok("User updated successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-all-users")
    public ResponseEntity<Page<WhoAmIDto>> getAllUsers(
            @RequestParam(required = false) String identification,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "0") int size
    ) {
        return ResponseEntity.ok(service.getAllUsers(identification,page,size));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-user-sessions")
    public ResponseEntity<List<MySessionsDto>> getUserSessions(
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(service.getMySessions(userId));
    }
}
