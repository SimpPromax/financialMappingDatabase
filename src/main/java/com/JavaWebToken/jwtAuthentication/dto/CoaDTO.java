package com.JavaWebToken.jwtAuthentication.dto;



import lombok.Data;

@Data
public class CoaDTO {
    private Long coaId;
    private String coaCode;
    private String coaName;
    private String description;
    private String sqlScript;
    private String createdBy;
}

