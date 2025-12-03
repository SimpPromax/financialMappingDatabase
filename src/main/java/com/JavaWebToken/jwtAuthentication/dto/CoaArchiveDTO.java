package com.JavaWebToken.jwtAuthentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CoaArchiveDTO {
    @NotBlank(message = "Archived by is required")
    private String archivedBy;

    private String reason; // Optional reason for archiving
}