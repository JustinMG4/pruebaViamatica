package com.justindev.prueba_telconet.modules.users.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyCodeDto {
    @NotNull
    private String email;

    @NotNull
    private String code;
}
