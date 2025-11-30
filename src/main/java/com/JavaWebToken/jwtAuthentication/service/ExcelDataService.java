package com.JavaWebToken.jwtAuthentication.service;

import com.JavaWebToken.jwtAuthentication.dto.ExcelElementDTO;
import com.JavaWebToken.jwtAuthentication.dto.ExcelSheetResponseDTO;
import com.JavaWebToken.jwtAuthentication.entity.ExcelElement;
import com.JavaWebToken.jwtAuthentication.entity.ExcelSheet;
import com.JavaWebToken.jwtAuthentication.repository.ExcelElementRepository;
import com.JavaWebToken.jwtAuthentication.repository.ExcelSheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExcelDataService {

    @Autowired
    private ExcelSheetRepository sheetRepo;

    @Autowired
    private ExcelElementRepository elementRepo;

    public ExcelSheet saveSheet(ExcelSheet sheet, String username) {
        ExcelSheet existing = sheetRepo.findByExcellSheetName(sheet.getExcellSheetName()).orElse(null);

        if (existing != null) {
            return updateExistingSheet(existing, sheet, username);
        } else {
            return createNewSheet(sheet, username);
        }
    }

    private ExcelSheet updateExistingSheet(ExcelSheet existing, ExcelSheet newSheet, String username) {
        existing.setModifiedBy(username);

        // Remove old elements
        List<ExcelElement> elementsToRemove = new ArrayList<>(existing.getExcelElements());
        for (ExcelElement element : elementsToRemove) {
            existing.getExcelElements().remove(element);
            elementRepo.delete(element);
        }

        // Add new elements
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

        if (sheet.getExcelElements() != null) {
            for (ExcelElement element : sheet.getExcelElements()) {
                element.setSheet(sheet);
            }
        }

        return sheetRepo.save(sheet);
    }

    public List<ExcelSheet> getAllSheetsWithData() {
        List<ExcelSheet> sheets = sheetRepo.findAll();
        for (ExcelSheet sheet : sheets) {
            List<ExcelElement> elements = elementRepo.findBySheet_SheetIdOrderByExcelElement(sheet.getSheetId());
            sheet.setExcelElements(elements);
        }
        return sheets;
    }

    /**
     * Retrieves the count of elements for every saved Excel sheet.
     * Used for the client-side initial load optimization.
     *
     * @return A map where key is the sheet name (String) and value is the element count (Integer).
     */
    public Map<String, Integer> getAllSheetElementCounts() {
        // 1. Get all sheets (this fetches minimal data if not already configured for eager loading)
        List<ExcelSheet> sheets = sheetRepo.findAll();

        Map<String, Integer> countsMap = new HashMap<>();

        // 2. Iterate and count elements for each sheet
        for (ExcelSheet sheet : sheets) {
            // Find all elements for this sheet and get the size
            int count = elementRepo.findBySheet_SheetIdOrderByExcelElement(sheet.getSheetId()).size();

            // 3. Map sheet name to count
            countsMap.put(sheet.getExcellSheetName(), count);
        }

        return countsMap;
    }

    public List<ExcelSheetResponseDTO> getAllSheetsWithDataAsDTO() {
        List<ExcelSheet> sheets = getAllSheetsWithData();

        return sheets.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    private ExcelSheetResponseDTO convertToResponseDTO(ExcelSheet sheet) {
        List<ExcelElementDTO> elementDTOs = new ArrayList<>();
        if (sheet.getExcelElements() != null) {
            elementDTOs = sheet.getExcelElements().stream()
                    .map(element -> {
                        ExcelElementDTO dto = new ExcelElementDTO();
                        dto.setElementId(element.getElementId());
                        dto.setExcelElement(element.getExcelElement());
                        dto.setExelCellValue(element.getExelCellValue());
                        dto.setCellReference(element.getCellReference());
                        return dto;
                    })
                    .collect(Collectors.toList());
        }

        ExcelSheetResponseDTO responseDTO = new ExcelSheetResponseDTO();
        responseDTO.setSheetId(sheet.getSheetId());
        responseDTO.setOriginalSheetName(sheet.getExcellSheetName()); // Or keep null if original name is unused
        responseDTO.setExcellSheetName(sheet.getExcellSheetName());
        responseDTO.setExcelElements(elementDTOs);

        return responseDTO;
    }

    public List<ExcelElementDTO> getElementsBySheetId(Long sheetId) {
        return elementRepo.findBySheet_SheetIdOrderByExcelElement(sheetId)
                .stream()
                .map(element -> {
                    ExcelElementDTO dto = new ExcelElementDTO();
                    dto.setElementId(element.getElementId());
                    dto.setExcelElement(element.getExcelElement());
                    dto.setExelCellValue(element.getExelCellValue());
                    dto.setCellReference(element.getCellReference());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Return all sheet names (just names, no DTO)
    public List<String> getAllSheetNames() {
        return sheetRepo.findAll()
                .stream()
                .map(ExcelSheet::getExcellSheetName)
                .toList();
    }

    // Return elements by sheet name (old method)
    public List<ExcelElementDTO> getElementsBySheetName(String sheetName) {
        return sheetRepo.findByExcellSheetName(sheetName)
                .map(sheet -> elementRepo.findBySheet_SheetIdOrderByExcelElement(sheet.getSheetId())
                        .stream()
                        .map(element -> {
                            ExcelElementDTO dto = new ExcelElementDTO();
                            dto.setElementId(element.getElementId());
                            dto.setExcelElement(element.getExcelElement());
                            dto.setExelCellValue(element.getExelCellValue());
                            dto.setCellReference(element.getCellReference());
                            return dto;
                        })
                        .toList()
                )
                .orElse(List.of());
    }

}