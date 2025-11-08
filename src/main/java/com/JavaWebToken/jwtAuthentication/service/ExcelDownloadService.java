package com.JavaWebToken.jwtAuthentication.service;

import com.JavaWebToken.jwtAuthentication.entity.UploadedFile;
import com.JavaWebToken.jwtAuthentication.repository.UploadedFileRepository;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class ExcelDownloadService {

    @Value("${upload.dir:${user.home}/excel-uploads}")
    private String uploadBaseDir;

    private final UploadedFileRepository uploadedFileRepository;

    public ExcelDownloadService(UploadedFileRepository uploadedFileRepository) {
        this.uploadedFileRepository = uploadedFileRepository;
    }

    public String processExcelUpload(MultipartFile excelFile) throws IOException {
        // === Validate input ===
        String originalName = excelFile.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        String ext = getFileExtension(originalName).toLowerCase();
        if (!ext.equals(".xlsx") && !ext.equals(".xls")) {
            throw new IllegalArgumentException("Only Excel files (.xlsx, .xls) are allowed.");
        }

        if (excelFile.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 10MB limit.");
        }

        // === Generate unique, safe filename ===
        String safeOriginal = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueFilename = timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + safeOriginal;

        // === Resolve safe upload directory ===
        Path uploadDir = Paths.get(uploadBaseDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path savePath = uploadDir.resolve(uniqueFilename);

        // === Save file to disk ===
        try (InputStream is = excelFile.getInputStream()) {
            Files.copy(is, savePath);
        }

        // === Determine if it's a workbook (multi-sheet) ===
        boolean isWorkbook = isExcelWorkbook(excelFile);

        // === Save metadata to database ===
        UploadedFile uploadedFile = new UploadedFile(originalName, savePath.toString(), isWorkbook);
        uploadedFileRepository.save(uploadedFile);

        return "File uploaded successfully! (Type: " + (isWorkbook ? "Workbook" : "Worksheet") + ")";
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex > 0) ? filename.substring(lastDotIndex) : "";
    }

    private boolean isExcelWorkbook(MultipartFile file) {
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            return workbook.getNumberOfSheets() > 1;
        } catch (Exception e) {
            return false;
        }
    }

    public List<UploadedFile> getUploadedFiles() {
        return uploadedFileRepository.findAllByOrderByDownloadDateDesc();
    }

    public void deleteFile(String fileName) throws IOException {
        UploadedFile fileRecord = uploadedFileRepository.findByFileName(fileName)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileName));

        // Delete physical file
        Path filePath = Paths.get(fileRecord.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // Delete from DB
        uploadedFileRepository.delete(fileRecord);
    }
}