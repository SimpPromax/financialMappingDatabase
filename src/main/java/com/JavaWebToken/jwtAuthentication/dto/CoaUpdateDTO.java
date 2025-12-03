package com.JavaWebToken.jwtAuthentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CoaUpdateDTO {
    @NotBlank(message = "COA Code is required")
    private String coaCode;

    @NotBlank(message = "COA Name is required")
    private String coaName;

    private String description;

    @NotBlank(message = "SQL Script is required")
    private String sqlScript;

    private String modifiedBy;
}