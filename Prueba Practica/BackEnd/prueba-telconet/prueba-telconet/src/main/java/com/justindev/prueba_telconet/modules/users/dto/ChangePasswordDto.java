package com.justindev.prueba_telconet.modules.users.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangePasswordDto {
    @NotNull
    private String newPassword;

    @NotNull
    private String confirmPassword;
}
