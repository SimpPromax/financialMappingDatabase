package com.JavaWebToken.jwtAuthentication.entity;



import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coa_id")
    private Long coaId;

    @Column(name = "coa_code", nullable = false)
    private String coaCode;

    @Column(name = "coa_name", nullable = false)
    private String coaName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(name = "sql_script", nullable = false)
    private String sqlScript;

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

