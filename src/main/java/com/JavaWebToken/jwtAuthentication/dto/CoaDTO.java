package com.JavaWebToken.jwtAuthentication.dto;

import com.JavaWebToken.jwtAuthentication.entity.Coa;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CoaDTO {
    private Long coaId;
    private String coaCode;
    private String coaName;
    private String description;
    private String sqlScript;
    private String createdBy;

    // Archive fields
    private Boolean archived;
    private String archivedBy;
    private LocalDateTime archivedDate;

    // Additional metadata
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;

    // Helper method to convert Entity to DTO
    public static CoaDTO fromEntity(Coa coa) {
        CoaDTO dto = new CoaDTO();
        dto.setCoaId(coa.getCoaId());
        dto.setCoaCode(coa.getCoaCode());
        dto.setCoaName(coa.getCoaName());
        dto.setDescription(coa.getDescription());
        dto.setSqlScript(coa.getSqlScript());
        dto.setCreatedBy(coa.getCreatedBy());
        dto.setArchived(coa.getArchived());
        dto.setArchivedBy(coa.getArchivedBy());
        dto.setArchivedDate(coa.getArchivedDate());
        dto.setCreatedDate(coa.getCreatedDate());
        dto.setModifiedBy(coa.getModifiedBy());
        dto.setModifiedDate(coa.getModifiedDate());
        return dto;
    }

    // Helper method to convert DTO to Entity for creation
    public static Coa toEntity(CoaDTO dto) {
        return Coa.builder()
                .coaCode(dto.getCoaCode())
                .coaName(dto.getCoaName())
                .description(dto.getDescription())
                .sqlScript(dto.getSqlScript())
                .createdBy(dto.getCreatedBy())
                .build();
    }

    // Check if COA is archived
    public boolean isArchived() {
        return archived != null && archived;
    }

    // Get formatted archived date
    public String getFormattedArchivedDate() {
        if (archivedDate == null) return "N/A";
        return archivedDate.toString(); // Or format as needed
    }

    // Get formatted created date
    public String getFormattedCreatedDate() {
        if (createdDate == null) return "N/A";
        return createdDate.toString(); // Or format as needed
    }
}