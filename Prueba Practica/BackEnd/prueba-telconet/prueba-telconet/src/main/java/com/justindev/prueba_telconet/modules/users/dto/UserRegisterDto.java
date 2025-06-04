package com.justindev.prueba_telconet.modules.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDto {

    @NotBlank
    @Email
    private String personalEmail;

    @NotBlank
    @NotNull
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank
    @NotNull
    @Size(min = 2, max = 50)
    private String lastname;

    @NotBlank
    @NotNull
    @Size(min = 2, max = 10)
    private String identification;

    @NotBlank
    @NotNull
    private String phone;

    @NotBlank
    @NotNull
    private String address;
}
