package com.JavaWebToken.jwtAuthentication.controller;

import com.JavaWebToken.jwtAuthentication.dto.ExcelSheetResponseDTO;
import com.JavaWebToken.jwtAuthentication.dto.ExcelElementDTO;
import com.JavaWebToken.jwtAuthentication.entity.ExcelElement;
import com.JavaWebToken.jwtAuthentication.entity.ExcelSheet;
import com.JavaWebToken.jwtAuthentication.service.ExcelDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/excel")
public class ExcelDataController {

    @Autowired
    private ExcelDataService excelDataService;

    @PostMapping("/save")
    public ResponseEntity<?> saveExcelData(@RequestBody List<ExcelSheet> excelSheets) {
        try {
            String username = "Anonymous";

            System.out.println("📥 Received " + excelSheets.size() + " sheets to save");

            for (ExcelSheet sheet : excelSheets) {
                System.out.println("📋 Processing sheet: " + sheet.getExcellSheetName());
                System.out.println("📝 Elements count: " + (sheet.getExcelElements() != null ? sheet.getExcelElements().size() : 0));

                excelDataService.saveSheet(sheet, username);
            }

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Excel data saved successfully!");
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            System.err.println("❌ Error saving Excel data: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("message", "Error saving data: " + e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    @GetMapping("/sheets")
    public ResponseEntity<List<String>> getExcelSheets() {
        List<String> sheetNames = excelDataService.getAllSheetNames();
        System.out.println("📋 Returning " + sheetNames.size() + " sheet names");
        return ResponseEntity.ok(sheetNames);
    }

    // NEW: Get data using DTOs (to fix circular reference)
    @GetMapping("/data")
    public ResponseEntity<List<ExcelSheetResponseDTO>> getExcelData() {
        try {
            List<ExcelSheetResponseDTO> sheets = excelDataService.getAllSheetsWithDataAsDTO();
            System.out.println("📊 Returning " + sheets.size() + " sheets with data (DTO format)");
            return ResponseEntity.ok(sheets);
        } catch (Exception e) {
            System.err.println("❌ Error fetching excel data with DTO: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // KEEP: Original method for backward compatibility (if needed)
    @GetMapping("/data/original")
    public ResponseEntity<List<ExcelSheet>> getExcelDataOriginal() {
        List<ExcelSheet> sheets = excelDataService.getAllSheetsWithData();
        System.out.println("📊 Returning " + sheets.size() + " sheets with data (original format)");
        return ResponseEntity.ok(sheets);
    }

    @GetMapping("/elements")
    public ResponseEntity<List<ExcelElementDTO>> getExcelElementsBySheetName(@RequestParam String sheetName) {
        try {
            // Service already returns DTOs
            List<ExcelElementDTO> elementDTOs = excelDataService.getElementsBySheetName(sheetName);

            System.out.println("🔍 Returning " + elementDTOs.size() + " elements for sheet: " + sheetName);
            return ResponseEntity.ok(elementDTOs);

        } catch (Exception e) {
            System.err.println("❌ Error fetching elements for sheet " + sheetName + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * NEW: Endpoint to get the count of elements for all sheets in one request.
     * The service layer is assumed to return a Map where key is the sheet name and value is the element count.
     */
    @GetMapping("/all-counts")
    public ResponseEntity<Map<String, Integer>> getAllSheetElementCounts() {
        try {
            // Assumed service method to retrieve a map of {sheetName: elementCount}
            Map<String, Integer> counts = excelDataService.getAllSheetElementCounts();
            System.out.println("🔢 Returning element counts for " + counts.size() + " sheets.");
            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            System.err.println("❌ Error fetching all element counts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

}