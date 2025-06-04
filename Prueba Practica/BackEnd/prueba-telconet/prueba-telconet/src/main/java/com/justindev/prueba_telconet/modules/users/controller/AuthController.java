package com.justindev.prueba_telconet.modules.users.controller;

import com.justindev.prueba_telconet.modules.users.dto.AuthResponseDto;
import com.justindev.prueba_telconet.modules.users.dto.LoginDto;
import com.justindev.prueba_telconet.modules.users.dto.WhoAmIDto;
import com.justindev.prueba_telconet.modules.users.model.AppUser;
import com.justindev.prueba_telconet.modules.users.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for authentication")
public class AuthController {

    private final AuthService service;


    @PreAuthorize("permitAll()")
    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Login with username and password",
            tags = {"Authentication"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Authentication data with username and password",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User logged successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponseDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid username or password",
                            content = @Content(mediaType = "application/json")

                    )
            }
    )
    public ResponseEntity<AuthResponseDto> login(
            @RequestBody @Valid LoginDto dto
    ) {
        return ResponseEntity.ok(service.login(dto));
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/whoami")
    @Operation(
            summary = "Who am I?",
            description = "Get the authenticated user",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User data",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WhoAmIDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized, user not authenticated",
                            content = @Content(mediaType = "application/json")

                    )
            }
    )
    public ResponseEntity<WhoAmIDto> whoAmI() {
        return ResponseEntity.ok(service.getAuthenticatedUser());
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/is-first-login")
    public ResponseEntity<Boolean> isFirstLogin(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(service.isFirstLogin(email));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @AuthenticationPrincipal AppUser user
    ) {
        service.registerLogout(user.getId());
        return ResponseEntity.ok("User logged out successfully");
    }
}
