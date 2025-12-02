package com.JavaWebToken.jwtAuthentication.service;

import com.JavaWebToken.jwtAuthentication.dto.CellMappingDTO;
import com.JavaWebToken.jwtAuthentication.entity.UploadedFile;
import com.JavaWebToken.jwtAuthentication.repository.UploadedFileRepository;
import com.JavaWebToken.jwtAuthentication.repository.ReportJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelReportService {

    private final ReportJdbcRepository reportRepository;
    private final UploadedFileRepository uploadedFileRepository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${upload.dir:${user.home}/excel-uploads}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public List<String> getAllSheetNames() {
        try {
            List<String> excelSheets = reportRepository.getAllSheetNames();

            if (excelSheets.isEmpty()) {
                log.info("No sheets in excel_sheets table, checking uploaded files...");
                List<UploadedFile> uploads = uploadedFileRepository.findAll();
                return uploads.stream()
                        .map(UploadedFile::getFileName)
                        .filter(name -> name.toLowerCase().endsWith(".xlsx") ||
                                name.toLowerCase().endsWith(".xls"))
                        .map(name -> {
                            // Extract base name without timestamp and GUID
                            return extractBaseSheetName(name);
                        })
                        .distinct()
                        .sorted()
                        .toList();
            }

            return excelSheets;
        } catch (Exception e) {
            log.error("Error getting sheet names: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // Helper to extract base sheet name from filename
    private String extractBaseSheetName(String fileName) {
        // Remove extension
        String nameWithoutExt = fileName
                .replace(".xlsx", "")
                .replace(".XLSX", "")
                .replace(".xls", "")
                .replace(".XLS", "");

        // Remove timestamp and GUID pattern: YYYYMMDD_HHMMSS_GUID_
        String pattern = "^\\d{8}_\\d{6}_[a-f0-9]+_";
        String cleanName = nameWithoutExt.replaceAll(pattern, "");

        // If pattern not found, return original name
        return cleanName.isEmpty() ? nameWithoutExt : cleanName;
    }

    @Transactional(readOnly = true)
    public List<CellMappingDTO> getMappingsForSheet(String sheetName) {
        log.info("Fetching mappings for sheet: {}", sheetName);
        try {
            // First try with the exact sheet name
            List<CellMappingDTO> mappings = reportRepository.getCellMappings(sheetName);

            if (mappings.isEmpty()) {
                // Try with base name (without extension)
                String baseName = sheetName
                        .replace(".xlsx", "")
                        .replace(".xls", "")
                        .replace(".XLSX", "")
                        .replace(".XLS", "");
                mappings = reportRepository.getCellMappings(baseName);

                if (mappings.isEmpty()) {
                    // Try to find any mapping that contains the sheet name
                    List<CellMappingDTO> allMappings = reportRepository.getAllCellMappings();
                    mappings = allMappings.stream()
                            .filter(m -> m.getSheetName() != null &&
                                    m.getSheetName().toLowerCase().contains(baseName.toLowerCase()))
                            .toList();
                }
            }

            log.info("Found {} mappings for sheet: {}", mappings.size(), sheetName);
            return mappings;

        } catch (Exception e) {
            log.error("Error fetching mappings for sheet {}: {}", sheetName, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public List<CellMappingDTO> getMappingsForSheetId(Long sheetId) {
        log.info("Fetching mappings for sheet ID: {}", sheetId);
        try {
            return reportRepository.getCellMappingsBySheetId(sheetId);
        } catch (Exception e) {
            log.error("Error fetching mappings for sheet ID {}: {}", sheetId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Transactional
    public Map<String, Object> generateReportData(String sheetName,
                                                  LocalDate startDate,
                                                  LocalDate endDate) {
        // Clean the sheet name - remove extension and timestamp/GUID if present
        String cleanSheetName = extractBaseSheetName(sheetName);

        List<CellMappingDTO> mappings = getMappingsForSheet(cleanSheetName);
        Map<String, Object> cellValues = new LinkedHashMap<>();

        if (mappings.isEmpty()) {
            log.warn("No mappings found for sheet: {} (clean name: {})", sheetName, cleanSheetName);
            return cellValues;
        }

        log.info("Generating data for {} mappings for sheet: {} (clean: {}), period: {} to {}",
                mappings.size(), sheetName, cleanSheetName, startDate, endDate);

        for (CellMappingDTO mapping : mappings) {
            try {
                if (mapping.getTargetCell() == null || mapping.getTargetCell().trim().isEmpty()) {
                    log.warn("Skipping mapping {} - no target cell defined", mapping.getMappingId());
                    continue;
                }

                if (mapping.getSqlScript() == null || mapping.getSqlScript().trim().isEmpty()) {
                    log.warn("Skipping mapping {} - no SQL script defined", mapping.getMappingId());
                    cellValues.put(mapping.getTargetCell(), "NO_SQL");
                    continue;
                }

                String executableSql = prepareSql(mapping.getSqlScript(), startDate, endDate);
                log.debug("Executing SQL for cell {}: {}", mapping.getTargetCell(),
                        executableSql.substring(0, Math.min(100, executableSql.length())) + "...");

                Object result = executeSql(executableSql, startDate, endDate);
                cellValues.put(mapping.getTargetCell(), result);

                log.debug("✅ Cell {} [{}] = {}", mapping.getTargetCell(),
                        mapping.getCoaName(), result);

            } catch (Exception e) {
                log.error("❌ Error processing cell {}: {}", mapping.getTargetCell(), e.getMessage());
                cellValues.put(mapping.getTargetCell(), "ERROR: " + e.getMessage());
            }
        }

        log.info("✅ Generated {} cell values for sheet: {}", cellValues.size(), cleanSheetName);
        return cellValues;
    }

    @Transactional
    public byte[] generateExcelReport(String sheetOrFileName,
                                      LocalDate startDate,
                                      LocalDate endDate) throws IOException {
        log.info("Generating Excel report for: {}, dates: {} to {}",
                sheetOrFileName, startDate, endDate);

        File excelFile = findExcelFile(sheetOrFileName);

        if (excelFile == null || !excelFile.exists()) {
            throw new FileNotFoundException(
                    String.format("Excel file not found for '%s'. Checked in: %s",
                            sheetOrFileName, uploadDir)
            );
        }

        // Extract clean sheet name from filename
        String cleanSheetName = extractBaseSheetName(excelFile.getName());
        log.info("Processing file: {}, clean sheet name: {}", excelFile.getName(), cleanSheetName);

        Map<String, Object> cellValues = generateReportData(cleanSheetName, startDate, endDate);

        if (cellValues.isEmpty()) {
            log.warn("No data generated for sheet: {}. Returning template as-is.", cleanSheetName);
            // Return the template file without modifications
            try (FileInputStream fis = new FileInputStream(excelFile);
                 ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                return bos.toByteArray();
            }
        }

        return updateExcelFile(excelFile, cellValues);
    }

    private String prepareSql(String sqlScript, LocalDate startDate, LocalDate endDate) {
        if (sqlScript == null || sqlScript.trim().isEmpty()) {
            return "";
        }

        // First replace named parameters
        String sql = sqlScript
                .replace("${startDate}", "'" + startDate + "'")
                .replace("${endDate}", "'" + endDate + "'")
                .replace(":startDate", "'" + startDate + "'")
                .replace(":endDate", "'" + endDate + "'")
                .replace(":year", "'" + startDate.getYear() + "'");

        // Also replace SQL Server style parameters
        sql = sql.replace("@startDate", "'" + startDate + "'")
                .replace("@endDate", "'" + endDate + "'")
                .replace("@year", "'" + startDate.getYear() + "'");

        // Validate it's a SELECT query
        String trimmedSql = sql.trim().toUpperCase();
        if (!trimmedSql.startsWith("SELECT")) {
            log.warn("SQL is not a SELECT query: {}", sql.substring(0, Math.min(50, sql.length())));
            // Still return it, let executeSql handle the error
        }

        return sql;
    }

    private Object executeSql(String sql, LocalDate startDate, LocalDate endDate) {
        try {
            if (sql == null || sql.trim().isEmpty()) {
                return null;
            }

            // Check for parameter placeholders
            boolean hasParameters = sql.contains("?");

            if (hasParameters) {
                // Handle prepared statements with parameters
                if (sql.toUpperCase().contains("SUM(") ||
                        sql.toUpperCase().contains("AVG(") ||
                        sql.toUpperCase().contains("COUNT(") ||
                        sql.toUpperCase().contains("MAX(") ||
                        sql.toUpperCase().contains("MIN(")) {

                    return jdbcTemplate.queryForObject(sql,
                            new Object[]{Date.valueOf(startDate), Date.valueOf(endDate)},
                            BigDecimal.class);
                } else {
                    return jdbcTemplate.queryForObject(sql,
                            new Object[]{Date.valueOf(startDate), Date.valueOf(endDate)},
                            Object.class);
                }
            } else {
                // No parameters, execute directly
                if (sql.toUpperCase().contains("SUM(") ||
                        sql.toUpperCase().contains("AVG(") ||
                        sql.toUpperCase().contains("COUNT(") ||
                        sql.toUpperCase().contains("MAX(") ||
                        sql.toUpperCase().contains("MIN(")) {

                    BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class);
                    return result != null ? result.doubleValue() : 0.0;
                } else {
                    return jdbcTemplate.queryForObject(sql, Object.class);
                }
            }

        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            log.warn("No results returned for SQL: {}", sql.substring(0, Math.min(100, sql.length())));
            return null;
        } catch (org.springframework.jdbc.BadSqlGrammarException e) {
            log.error("Bad SQL grammar: {}", sql);
            log.error("SQL error details: {}", e.getMessage());
            return "ERROR: Bad SQL - " + e.getMostSpecificCause().getMessage();
        } catch (Exception e) {
            log.error("SQL execution failed: {}", sql, e);
            return "ERROR: " + e.getMessage();
        }
    }

    private File findExcelFile(String sheetName) {
        log.info("Looking for Excel file for: {}", sheetName);

        // Clean the sheet name
        String cleanSheetName = extractBaseSheetName(sheetName);

        // First, try to find exact match in database
        List<UploadedFile> allFiles = uploadedFileRepository.findAll();

        for (UploadedFile upload : allFiles) {
            String fileName = upload.getFileName();
            String fileNameWithoutExt = fileName
                    .replace(".xlsx", "")
                    .replace(".xls", "")
                    .replace(".XLSX", "")
                    .replace(".XLS", "");

            // Check if this file matches (with or without timestamp/GUID)
            if (fileNameWithoutExt.equalsIgnoreCase(sheetName) ||
                    fileNameWithoutExt.equalsIgnoreCase(cleanSheetName) ||
                    extractBaseSheetName(fileName).equalsIgnoreCase(cleanSheetName)) {

                File file = new File(upload.getFilePath());
                if (file.exists()) {
                    log.info("Found matching file in database: {}", upload.getFileName());
                    return file;
                } else {
                    log.warn("File in database doesn't exist: {}", upload.getFilePath());
                }
            }
        }

        // If not found in database, search in upload directory
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            log.error("Upload directory doesn't exist: {}", uploadDir);
            return null;
        }

        // Search for files with similar names
        File[] allExcelFiles = uploadDirectory.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".xlsx") || name.toLowerCase().endsWith(".xls")
        );

        if (allExcelFiles != null) {
            for (File file : allExcelFiles) {
                String fileName = file.getName();
                String baseName = extractBaseSheetName(fileName);

                if (baseName.equalsIgnoreCase(cleanSheetName)) {
                    log.info("Found file in upload directory: {}", file.getName());
                    return file;
                }
            }
        }

        log.error("Could not find Excel file for: {} (clean name: {})", sheetName, cleanSheetName);
        return null;
    }

    private byte[] updateExcelFile(File excelFile, Map<String, Object> cellValues) throws IOException {
        Workbook workbook;
        String fileName = excelFile.getName().toLowerCase();

        try (FileInputStream fis = new FileInputStream(excelFile)) {
            if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else {
                throw new IllegalArgumentException("Unsupported file format: " + fileName);
            }
        }

        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null) {
            sheet = workbook.createSheet("Report");
        }

        log.info("Updating {} cells in Excel file: {}", cellValues.size(), excelFile.getName());

        int updatedCount = 0;
        for (Map.Entry<String, Object> entry : cellValues.entrySet()) {
            String cellRef = entry.getKey().toUpperCase();
            Object value = entry.getValue();

            try {
                CellReference ref;
                try {
                    ref = new CellReference(cellRef);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid cell reference: {}", cellRef);
                    continue;
                }

                int rowIdx = ref.getRow();
                int colIdx = ref.getCol();

                Row row = sheet.getRow(rowIdx);
                if (row == null) {
                    row = sheet.createRow(rowIdx);
                }

                Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

                if (value == null) {
                    cell.setBlank();
                } else if (value instanceof Number) {
                    double doubleValue = ((Number) value).doubleValue();
                    cell.setCellValue(doubleValue);

                    // Apply number format for financial values
                    if (Math.abs(doubleValue) >= 1000) {
                        CellStyle numberStyle = workbook.createCellStyle();
                        DataFormat format = workbook.createDataFormat();
                        numberStyle.setDataFormat(format.getFormat("#,##0.00"));
                        cell.setCellStyle(numberStyle);
                    }
                } else if (value instanceof String) {
                    String strValue = (String) value;
                    if (strValue.startsWith("ERROR:")) {
                        cell.setCellValue(strValue);
                        CellStyle errorStyle = workbook.createCellStyle();
                        errorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                        errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        cell.setCellStyle(errorStyle);
                    } else {
                        cell.setCellValue(strValue);
                    }
                } else if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value);
                } else if (value instanceof Date) {
                    cell.setCellValue((Date) value);
                } else if (value instanceof LocalDate) {
                    cell.setCellValue((LocalDate) value);
                } else if (value instanceof LocalDateTime) {
                    cell.setCellValue((LocalDateTime) value);
                } else {
                    cell.setCellValue(value.toString());
                }

                updatedCount++;

            } catch (Exception e) {
                log.warn("Failed to update cell {}: {}", cellRef, e.getMessage());
            }
        }

        log.info("✅ Updated {} cells successfully in {}", updatedCount, excelFile.getName());

        // Write to byte array
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            byte[] bytes = bos.toByteArray();

            // Verify the file is not empty
            if (bytes.length == 0) {
                throw new IOException("Generated Excel file is empty");
            }

            log.info("Generated Excel file size: {} bytes", bytes.length);
            return bytes;
        } finally {
            workbook.close();
        }
    }

    @Transactional(readOnly = true)
    public File getExcelFileBySheetId(Long sheetId) {
        try {
            String sql = "SELECT excell_sheet_name FROM excel_sheets WHERE sheet_id = ?";
            String sheetName = jdbcTemplate.queryForObject(sql, String.class, sheetId);

            if (sheetName == null) {
                log.error("No sheet found with ID: {}", sheetId);
                return null;
            }

            return findExcelFile(sheetName);

        } catch (Exception e) {
            log.error("Error finding Excel file for sheet ID {}: {}", sheetId, e.getMessage());
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllExcelTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();

        List<UploadedFile> uploads = uploadedFileRepository.findAllByOrderByDownloadDateDesc();

        for (UploadedFile upload : uploads) {
            Map<String, Object> template = new HashMap<>();
            template.put("id", upload.getId());
            template.put("fileName", upload.getFileName());
            template.put("filePath", upload.getFilePath());
            template.put("uploadDate", upload.getDownloadDate());
            template.put("isWorkbook", upload.getIsWorkbook());

            File file = new File(upload.getFilePath());
            template.put("fileExists", file.exists());
            template.put("fileSize", file.exists() ? file.length() : 0);

            // Add the clean sheet name for display
            template.put("sheetName", extractBaseSheetName(upload.getFileName()));

            templates.add(template);
        }

        return templates;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTemplateInfo(String fileName) {
        // Try exact match first
        Optional<UploadedFile> upload = uploadedFileRepository.findByFileName(fileName);

        if (upload.isEmpty()) {
            // Try without extension
            String nameWithoutExt = fileName
                    .replace(".xlsx", "")
                    .replace(".xls", "")
                    .replace(".XLSX", "")
                    .replace(".XLS", "");

            List<UploadedFile> allFiles = uploadedFileRepository.findAll();
            upload = allFiles.stream()
                    .filter(f -> f.getFileName().toLowerCase().contains(nameWithoutExt.toLowerCase()))
                    .findFirst();
        }

        if (upload.isPresent()) {
            UploadedFile uploadedFile = upload.get();
            Map<String, Object> info = new HashMap<>();
            info.put("id", uploadedFile.getId());
            info.put("fileName", uploadedFile.getFileName());
            info.put("filePath", uploadedFile.getFilePath());
            info.put("uploadDate", uploadedFile.getDownloadDate());
            info.put("isWorkbook", uploadedFile.getIsWorkbook());

            File file = new File(uploadedFile.getFilePath());
            info.put("fileExists", file.exists());
            info.put("fileSize", file.exists() ? file.length() : 0);
            info.put("sheetName", extractBaseSheetName(uploadedFile.getFileName()));

            return info;
        }

        log.warn("Template info not found for: {}", fileName);
        return Collections.emptyMap();
    }

    // New method for preview (without writing to file)
    @Transactional
    public Map<String, Object> previewReportData(String sheetName,
                                                 LocalDate startDate,
                                                 LocalDate endDate) {
        return generateReportData(sheetName, startDate, endDate);
    }
}