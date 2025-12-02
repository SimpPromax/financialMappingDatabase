package com.JavaWebToken.jwtAuthentication.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "excel_downloads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "download_date", nullable = false)
    private LocalDateTime downloadDate;

    @Column(name = "is_workbook", nullable = false)
    private Boolean isWorkbook;

    @PrePersist
    public void prePersist() {
        if (downloadDate == null) {
            downloadDate = LocalDateTime.now();
        }
    }

    // Custom constructor without id (keep if needed)
    public UploadedFile(String fileName, String filePath, Boolean isWorkbook) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.isWorkbook = isWorkbook;
        this.downloadDate = LocalDateTime.now();
    }
}