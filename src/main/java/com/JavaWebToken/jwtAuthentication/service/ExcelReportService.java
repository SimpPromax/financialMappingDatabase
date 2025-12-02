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
            // Get exact sheet names from excel_sheets table
            List<String> excelSheets = reportRepository.getAllSheetNames();

            if (excelSheets.isEmpty()) {
                log.info("No sheets in excel_sheets table, checking uploaded files...");
                List<UploadedFile> uploads = uploadedFileRepository.findAll();
                return uploads.stream()
                        .map(UploadedFile::getFileName)
                        .filter(name -> name.toLowerCase().endsWith(".xlsx") ||
                                name.toLowerCase().endsWith(".xls"))
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

    @Transactional(readOnly = true)
    public List<CellMappingDTO> getMappingsForSheet(String sheetName) {
        log.info("Fetching mappings for exact sheet name: {}", sheetName);
        try {
            // Query with EXACT sheet name from excel_sheets table
            List<CellMappingDTO> mappings = reportRepository.getCellMappings(sheetName);

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
        // Use EXACT sheet name as it appears in excel_sheets table
        log.info("Generating data for exact sheet: {}, period: {} to {}",
                sheetName, startDate, endDate);

        List<CellMappingDTO> mappings = getMappingsForSheet(sheetName);
        Map<String, Object> cellValues = new LinkedHashMap<>();

        if (mappings.isEmpty()) {
            log.warn("No mappings found for sheet: {}", sheetName);
            return cellValues;
        }

        log.info("Generating data for {} mappings for sheet: {}",
                mappings.size(), sheetName);

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

        log.info("✅ Generated {} cell values for sheet: {}", cellValues.size(), sheetName);
        return cellValues;
    }

    @Transactional
    public byte[] generateExcelReport(String sheetName,
                                      LocalDate startDate,
                                      LocalDate endDate) throws IOException {
        log.info("Generating Excel report for sheet name: {}, dates: {} to {}",
                sheetName, startDate, endDate);

        // Find the actual Excel file using EXACT sheet name
        File excelFile = findExcelFile(sheetName);

        if (excelFile == null || !excelFile.exists()) {
            throw new FileNotFoundException(
                    String.format("Excel file not found for sheet '%s'. Checked in: %s",
                            sheetName, uploadDir)
            );
        }

        log.info("Found Excel file: {} for sheet name: {}",
                excelFile.getAbsolutePath(), sheetName);

        // Generate data using EXACT sheet name
        Map<String, Object> cellValues = generateReportData(sheetName, startDate, endDate);

        if (cellValues.isEmpty()) {
            log.warn("No data generated for sheet: {}. Returning template as-is.", sheetName);
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

    // SIMPLIFIED findExcelFile - uses exact names
    private File findExcelFile(String sheetName) {
        log.info("Looking for Excel file for sheet name: {}", sheetName);

        // 1. First, search in uploadedFileRepository by exact fileName
        Optional<UploadedFile> upload = uploadedFileRepository.findByFileName(sheetName);

        if (upload.isPresent()) {
            File file = new File(upload.get().getFilePath());
            if (file.exists()) {
                log.info("Found exact match in uploaded files: {}", file.getAbsolutePath());
                return file;
            } else {
                log.warn("File in database doesn't exist: {}", upload.get().getFilePath());
            }
        }

        // 2. Search for files containing the sheet name
        List<UploadedFile> allFiles = uploadedFileRepository.findAll();

        for (UploadedFile uploadFile : allFiles) {
            String dbFileName = uploadFile.getFileName();
            String dbFilePath = uploadFile.getFilePath();

            // Check if this is the file we're looking for
            if (dbFileName.equalsIgnoreCase(sheetName)) {
                File file = new File(dbFilePath);
                if (file.exists()) {
                    log.info("Found case-insensitive match: {}", dbFilePath);
                    return file;
                }
            }
        }

        // 3. If not found in database, search in upload directory
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            log.error("Upload directory doesn't exist: {}", uploadDir);
            return null;
        }

        // Search for files with exact name match
        File[] allExcelFiles = uploadDirectory.listFiles((dir, name) ->
                name.equalsIgnoreCase(sheetName) ||
                        name.replace(".xlsx", "").replace(".xls", "")
                                .equalsIgnoreCase(sheetName.replace(".xlsx", "").replace(".xls", "")));

        if (allExcelFiles != null && allExcelFiles.length > 0) {
            log.info("Found file in upload directory: {}", allExcelFiles[0].getAbsolutePath());
            return allExcelFiles[0];
        }

        log.error("Could not find Excel file for sheet name: {}", sheetName);
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
            template.put("fileName", upload.getFileName());  // EXACT filename
            template.put("filePath", upload.getFilePath());
            template.put("uploadDate", upload.getDownloadDate());
            template.put("isWorkbook", upload.getIsWorkbook());

            File file = new File(upload.getFilePath());
            template.put("fileExists", file.exists());
            template.put("fileSize", file.exists() ? file.length() : 0);

            // Use exact fileName as sheetName
            template.put("sheetName", upload.getFileName());

            templates.add(template);
        }

        return templates;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTemplateInfo(String fileName) {
        // Try exact match first
        Optional<UploadedFile> upload = uploadedFileRepository.findByFileName(fileName);

        if (upload.isPresent()) {
            UploadedFile uploadedFile = upload.get();
            Map<String, Object> info = new HashMap<>();
            info.put("id", uploadedFile.getId());
            info.put("fileName", uploadedFile.getFileName());  // EXACT name
            info.put("filePath", uploadedFile.getFilePath());
            info.put("uploadDate", uploadedFile.getDownloadDate());
            info.put("isWorkbook", uploadedFile.getIsWorkbook());

            File file = new File(uploadedFile.getFilePath());
            info.put("fileExists", file.exists());
            info.put("fileSize", file.exists() ? file.length() : 0);
            info.put("sheetName", uploadedFile.getFileName());  // EXACT name

            return info;
        }

        log.warn("Template info not found for: {}", fileName);
        return Collections.emptyMap();
    }
}