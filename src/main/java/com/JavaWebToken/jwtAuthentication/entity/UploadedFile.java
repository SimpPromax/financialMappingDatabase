package com.JavaWebToken.jwtAuthentication.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "excel_downloads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private LocalDateTime downloadDate;

    @Column(nullable = false)
    private Boolean isWorkbook;

    // Custom constructor without id
    public UploadedFile(String fileName, String filePath, Boolean isWorkbook) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.isWorkbook = isWorkbook;
        this.downloadDate = LocalDateTime.now();
    }
}