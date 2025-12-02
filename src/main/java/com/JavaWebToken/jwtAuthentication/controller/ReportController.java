package com.JavaWebToken.jwtAuthentication.controller;

import com.JavaWebToken.jwtAuthentication.dto.CellMappingDTO;
import com.JavaWebToken.jwtAuthentication.dto.ReportRequestDTO;
import com.JavaWebToken.jwtAuthentication.service.ExcelReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Excel Reports", description = "Excel Report Generation APIs")
public class ReportController {

    private final ExcelReportService excelReportService;

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new java.util.HashMap<>();
        response.put("status", "UP");
        response.put("service", "Excel Report Generator");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/templates")
    @Operation(summary = "Get all Excel templates")
    public ResponseEntity<List<Map<String, Object>>> getAllTemplates() {
        try {
            List<Map<String, Object>> templates = excelReportService.getAllExcelTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            log.error("Error fetching templates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/template/{fileName}")
    @Operation(summary = "Get template information")
    public ResponseEntity<Map<String, Object>> getTemplateInfo(@PathVariable String fileName) {
        try {
            Map<String, Object> templateInfo = excelReportService.getTemplateInfo(fileName);
            if (templateInfo.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(templateInfo);
        } catch (Exception e) {
            log.error("Error fetching template info for {}: {}", fileName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/sheets")
    @Operation(summary = "Get all sheet names")
    public ResponseEntity<List<String>> getAllSheetNames() {
        try {
            List<String> sheetNames = excelReportService.getAllSheetNames();
            return ResponseEntity.ok(sheetNames);
        } catch (Exception e) {
            log.error("Error fetching sheet names: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/mappings/{sheetName}")
    @Operation(summary = "Get cell mappings for a sheet")
    public ResponseEntity<List<CellMappingDTO>> getMappings(@PathVariable String sheetName) {
        try {
            List<CellMappingDTO> mappings = excelReportService.getMappingsForSheet(sheetName);
            if (mappings.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(mappings);
        } catch (Exception e) {
            log.error("Error fetching mappings for sheet {}: {}", sheetName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/mappings/sheet/{sheetId}")
    @Operation(summary = "Get cell mappings by sheet ID")
    public ResponseEntity<List<CellMappingDTO>> getMappingsBySheetId(@PathVariable Long sheetId) {
        try {
            List<CellMappingDTO> mappings = excelReportService.getMappingsForSheetId(sheetId);
            if (mappings.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(mappings);
        } catch (Exception e) {
            log.error("Error fetching mappings for sheet ID {}: {}", sheetId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/preview/{sheetName}")
    @Operation(summary = "Preview report data")
    public ResponseEntity<Map<String, Object>> previewReport(
            @PathVariable String sheetName,
            @RequestBody ReportRequestDTO request) {

        try {
            log.info("Preview request for sheet: {}, dates: {} to {}",
                    sheetName, request.getStartDate(), request.getEndDate());

            Map<String, Object> previewData = excelReportService.generateReportData(
                    sheetName,
                    request.getStartDate(),
                    request.getEndDate()
            );

            return ResponseEntity.ok(previewData);

        } catch (Exception e) {
            log.error("Error generating preview for sheet {}: {}", sheetName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate/{sheetName}")
    @Operation(summary = "Generate and download Excel report")
    public ResponseEntity<Resource> generateExcelReport(
            @PathVariable String sheetName,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        try {
            log.info("Generating Excel report for sheet: {}, dates: {} to {}",
                    sheetName, startDate, endDate);

            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            if (start.isAfter(end)) {
                return ResponseEntity.badRequest().body(null);
            }

            byte[] excelData = excelReportService.generateExcelReport(sheetName, start, end);

            // Create download filename with the exact sheet name
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s_%s_to_%s.xlsx",
                    sheetName.replace(".xls", "").replace(".xlsx", ""),
                    start.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    end.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            ByteArrayResource resource = new ByteArrayResource(excelData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(excelData.length)
                    .body(resource);

        } catch (IOException e) {
            log.error("Excel file error for {}: {}", sheetName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument for {}: {}", sheetName, e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            log.error("Error generating report for {}: {}", sheetName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate Excel report with JSON body")
    public ResponseEntity<Resource> generateExcelReportJson(@RequestBody ReportRequestDTO request) {
        try {
            log.info("Generating report via JSON: {}, dates: {} to {}",
                    request.getSheetName(), request.getStartDate(), request.getEndDate());

            byte[] excelData = excelReportService.generateExcelReport(
                    request.getSheetName(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s_%s_to_%s.xlsx",
                    request.getSheetName().replace(".xls", "").replace(".xlsx", ""),
                    request.getStartDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                    request.getEndDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            ByteArrayResource resource = new ByteArrayResource(excelData);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(excelData.length)
                    .body(resource);

        } catch (IOException e) {
            log.error("Excel file error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Error generating report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/file/{sheetId}")
    @Operation(summary = "Get Excel file by sheet ID")
    public ResponseEntity<Resource> getExcelFileBySheetId(@PathVariable Long sheetId) {
        try {
            java.io.File excelFile = excelReportService.getExcelFileBySheetId(sheetId);

            if (excelFile == null || !excelFile.exists()) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = java.nio.file.Files.readAllBytes(excelFile.toPath());
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + excelFile.getName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileContent.length)
                    .body(resource);

        } catch (Exception e) {
            log.error("Error getting Excel file for sheet ID {}: {}", sheetId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}