package com.JavaWebToken.jwtAuthentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CoaRestoreDTO {
    @NotBlank(message = "Restored by is required")
    private String restoredBy;
}