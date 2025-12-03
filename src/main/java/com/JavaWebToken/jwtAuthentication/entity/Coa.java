package com.JavaWebToken.jwtAuthentication.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "coa")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE coa SET archived = true WHERE coa_id = ?")

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

    // Archive fields
    @Column(name = "archived", nullable = false)
    @Builder.Default
    private Boolean archived = false;

    @Column(name = "archived_by")
    private String archivedBy;

    @Column(name = "archived_date")
    private LocalDateTime archivedDate;

    // Add relationship to versions
    @OneToMany(mappedBy = "coa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CoaVersion> versions = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdDate == null) createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (archived == null) archived = false;
    }

    @PreUpdate
    public void preUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}