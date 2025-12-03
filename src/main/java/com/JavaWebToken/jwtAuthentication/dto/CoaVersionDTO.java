package com.JavaWebToken.jwtAuthentication.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CoaVersionDTO {
    private Long versionId;
    private Long coaId;
    private Integer versionNumber;
    private String coaCode;
    private String coaName;
    private String description;
    private String sqlScript;
    private String changedBy;
    private String changeType;
    private String changes;
    private LocalDateTime createdAt;
}