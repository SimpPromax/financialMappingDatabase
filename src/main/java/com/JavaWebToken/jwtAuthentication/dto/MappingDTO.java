package com.JavaWebToken.jwtAuthentication.dto;



import lombok.Data;

@Data
public class MappingDTO {
    private Long mappingId;
    private Long sheetId;
    private Long elementId;
    private Long coaId;
    private String createdBy;
}

