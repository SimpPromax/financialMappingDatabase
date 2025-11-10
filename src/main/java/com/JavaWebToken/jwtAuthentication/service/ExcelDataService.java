package com.JavaWebToken.jwtAuthentication.service;

import com.JavaWebToken.jwtAuthentication.dto.ExcelSheetResponseDTO;
import com.JavaWebToken.jwtAuthentication.dto.ExcelElementDTO;
import com.JavaWebToken.jwtAuthentication.entity.ExcelElement;
import com.JavaWebToken.jwtAuthentication.entity.ExcelSheet;
import com.JavaWebToken.jwtAuthentication.repository.ExcelElementRepository;
import com.JavaWebToken.jwtAuthentication.repository.ExcelSheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExcelDataService {

    @Autowired
    private ExcelSheetRepository sheetRepo;

    @Autowired
    private ExcelElementRepository elementRepo;

    public ExcelSheet saveSheet(ExcelSheet sheet, String username) {
        System.out.println("💾 Saving sheet: " + sheet.getExcellSheetName());

        ExcelSheet existing = sheetRepo.findByExcellSheetName(sheet.getExcellSheetName()).orElse(null);

        if (existing != null) {
            System.out.println("🔄 Updating existing sheet: " + existing.getSheetId());
            return updateExistingSheet(existing, sheet, username);
        } else {
            System.out.println("🆕 Creating new sheet");
            return createNewSheet(sheet, username);
        }
    }

    private ExcelSheet updateExistingSheet(ExcelSheet existing, ExcelSheet newSheet, String username) {
        existing.setModifiedBy(username);

        // SAFEST APPROACH: Use the existing collection instead of replacing it
        // Clear the existing collection manually (element by element)
        List<ExcelElement> elementsToRemove = new ArrayList<>(existing.getExcelElements());
        for (ExcelElement element : elementsToRemove) {
            existing.getExcelElements().remove(element);
            elementRepo.delete(element); // Manually delete each element
        }

        // Add new elements to the existing collection
        if (newSheet.getExcelElements() != null) {
            for (ExcelElement newElement : newSheet.getExcelElements()) {
                ExcelElement element = new ExcelElement();
                element.setExcelElement(newElement.getExcelElement());
                element.setExelCellValue(newElement.getExelCellValue());
                element.setCellReference(newElement.getCellReference());
                element.setSheet(existing);
                existing.getExcelElements().add(element);
            }
        }

        return sheetRepo.save(existing);
    }

    private ExcelSheet createNewSheet(ExcelSheet sheet, String username) {
        sheet.setCreatedBy(username);

        // Set relationship for all elements in new sheet
        if (sheet.getExcelElements() != null) {
            for (ExcelElement element : sheet.getExcelElements()) {
                element.setSheet(sheet);
            }
        }
        return sheetRepo.save(sheet);
    }

    public List<String> getAllSheetNames() {
        return sheetRepo.findAll()
                .stream()
                .map(ExcelSheet::getExcellSheetName)
                .toList();
    }

    public List<ExcelSheet> getAllSheetsWithData() {
        // Load all sheets
        List<ExcelSheet> sheets = sheetRepo.findAll();

        // Manually load elements for each sheet
        for (ExcelSheet sheet : sheets) {
            List<ExcelElement> elements = elementRepo.findBySheet_SheetIdOrderByExcelElement(sheet.getSheetId());
            sheet.setExcelElements(elements);
        }

        return sheets;
    }

    // NEW: Get all sheets with data using DTOs (to fix circular reference)
    public List<ExcelSheetResponseDTO> getAllSheetsWithDataAsDTO() {
        // Load all sheets with their elements
        List<ExcelSheet> sheets = getAllSheetsWithData();

        System.out.println("📊 Found " + sheets.size() + " sheets in database");

        List<ExcelSheetResponseDTO> result = sheets.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        System.out.println("✅ Converted to " + result.size() + " DTOs");
        return result;
    }

    private ExcelSheetResponseDTO convertToResponseDTO(ExcelSheet sheet) {
        if (sheet == null) {
            return null;
        }

        System.out.println("🔄 Converting sheet: " + sheet.getExcellSheetName() +
                " with " + (sheet.getExcelElements() != null ? sheet.getExcelElements().size() : 0) + " elements");

        List<ExcelElementDTO> elementDTOs = new ArrayList<>();
        if (sheet.getExcelElements() != null) {
            elementDTOs = sheet.getExcelElements().stream()
                    .map(element -> {
                        ExcelElementDTO dto = new ExcelElementDTO();
                        dto.setExcelElement(element.getExcelElement());
                        dto.setExelCellValue(element.getExelCellValue());
                        return dto;
                    })
                    .collect(Collectors.toList());
        }

        ExcelSheetResponseDTO responseDTO = new ExcelSheetResponseDTO();
        responseDTO.setOriginalSheetName(null); // Set if you have this data
        responseDTO.setExcellSheetName(sheet.getExcellSheetName());
        responseDTO.setExcelElements(elementDTOs);

        System.out.println("✅ Converted sheet to DTO: " + responseDTO.getExcellSheetName() +
                " with " + responseDTO.getExcelElements().size() + " elements");

        return responseDTO;
    }

    public List<ExcelElement> getElementsBySheetName(String sheetName) {
        return sheetRepo.findByExcellSheetName(sheetName)
                .map(sheet -> elementRepo.findBySheet_SheetIdOrderByExcelElement(sheet.getSheetId()))
                .orElse(List.of());
    }
}