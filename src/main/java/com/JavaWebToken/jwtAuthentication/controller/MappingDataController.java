package com.JavaWebToken.jwtAuthentication.controller;

import com.JavaWebToken.jwtAuthentication.dto.CoaDTO;
import com.JavaWebToken.jwtAuthentication.dto.ExcelElementDTO;
import com.JavaWebToken.jwtAuthentication.dto.ExcelSheetResponseDTO;
import com.JavaWebToken.jwtAuthentication.service.ExcelDataService;
import com.JavaWebToken.jwtAuthentication.service.MappedCellService;
import com.JavaWebToken.jwtAuthentication.service.CoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController

public class MappingDataController {

    @Autowired
    private ExcelDataService excelDataService;

    @Autowired
    private CoaService coaService;

    @Autowired
    private MappedCellService mappedCellService;

    // GET all Excel sheets
    @GetMapping("/api/excel-sheets")
    public ResponseEntity<List<ExcelSheetResponseDTO>> getExcelSheets() {
        try {
            List<ExcelSheetResponseDTO> sheets = excelDataService.getAllSheetsWithDataAsDTO();
            return ResponseEntity.ok(sheets);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }



    // GET Excel elements by sheetId
    @GetMapping("/api/excel-elements")
    public ResponseEntity<List<ExcelElementDTO>> getExcelElements(@RequestParam Long sheetId) {
        try {
            List<ExcelElementDTO> elements = excelDataService.getElementsBySheetId(sheetId);
            return ResponseEntity.ok(elements);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }


    // Add this to your existing MappingDataController
    @DeleteMapping("/api/mappings/{mappingId}")
    public ResponseEntity<?> deleteMapping(@PathVariable Long mappingId) {
        try {
            mappedCellService.deleteMapping(mappingId);
            return ResponseEntity.ok().body(Map.of("message", "Mapping deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete mapping: " + e.getMessage()));
        }
    }
}
