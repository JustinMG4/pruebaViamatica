package com.justindev.prueba_telconet.modules.users.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WhoAmIDto {
    private String name;
    private String lastname;
    private String email;
    private String identification;
    private String personalEmail;
    private String phone;
    private String address;
    private String username;
    private List<String> roles;
}
