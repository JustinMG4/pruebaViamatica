package com.justindev.prueba_telconet.modules.users.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MySessionsDto {
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private boolean active;

    // Additional fields can be added as needed


}
