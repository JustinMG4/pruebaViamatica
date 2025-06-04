package com.justindev.prueba_telconet.modules.users.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WhoamiDto {
    private String name;
    private String lastname;
    private String email;
    private String phone;
    private String address;
    private String username;
    private List<String> roles;
}
