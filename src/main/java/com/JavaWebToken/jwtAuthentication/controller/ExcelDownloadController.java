package com.JavaWebToken.jwtAuthentication.controller;

import com.JavaWebToken.jwtAuthentication.entity.UploadedFile;
import com.JavaWebToken.jwtAuthentication.service.ExcelDownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
@CrossOrigin(origins = "*")
public class ExcelDownloadController {

    @Autowired
    private ExcelDownloadService excelDownloadService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcelFile(
            @RequestParam("excelFile") MultipartFile excelFile) {
        try {
            String result = excelDownloadService.processExcelUpload(excelFile);
            return ResponseEntity.ok().body(Map.of("message", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/files")
    public ResponseEntity<List<UploadedFile>> getUploadedFiles() {
        try {
            List<UploadedFile> files = excelDownloadService.getUploadedFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/files")
    public ResponseEntity<?> deleteFile(@RequestBody Map<String, String> request) {
        try {
            String fileName = request.get("fileName");
            if (fileName == null || fileName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File name is required"));
            }
            excelDownloadService.deleteFile(fileName);
            return ResponseEntity.ok().body(Map.of("message", "File deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}