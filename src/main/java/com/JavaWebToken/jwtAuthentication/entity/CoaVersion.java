package com.JavaWebToken.jwtAuthentication.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coa_versions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoaVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "version_id")
    private Long versionId;

    @ManyToOne
    @JoinColumn(name = "coa_id", nullable = false)
    private Coa coa;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "coa_code", nullable = false)
    private String coaCode;

    @Column(name = "coa_name", nullable = false)
    private String coaName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(name = "sql_script", nullable = false, columnDefinition = "TEXT")
    private String sqlScript;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "change_type", nullable = false)
    private String changeType; // CREATE, UPDATE, DELETE

    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes; // JSON or text field to store what changed

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}