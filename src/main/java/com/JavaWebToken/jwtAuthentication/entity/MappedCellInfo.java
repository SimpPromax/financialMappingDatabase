package com.JavaWebToken.jwtAuthentication.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mapped_cell_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MappedCellInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long mappingId;

    @Column(name = "sheet_id", nullable = false)
    private Long sheetId;

    @Column(name = "element_id", nullable = false)
    private Long elementId;

    @Column(name = "coa_id", nullable = false)
    private Long coaId;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist
    public void prePersist() {
        if (createdDate == null) createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}

