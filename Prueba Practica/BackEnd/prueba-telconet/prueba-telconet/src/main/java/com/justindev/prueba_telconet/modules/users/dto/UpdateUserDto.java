package com.justindev.prueba_telconet.modules.users.dto;

import lombok.Data;

@Data
public class UpdateUserDto {
    private String name;
    private String lastname;
    private String personalEmail;
    private String phone;
    private String address;
    private String username;
}
