package com.JavaWebToken.jwtAuthentication.dto;

import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExcelSheetResponseDTO {

    @Setter
    private Long sheetId; // Added to use in frontend selection
    @Setter
    private String originalSheetName;
    @Setter
    private String excellSheetName;
    private List<ExcelElementDTO> excelElements;

    // Constructors
    public ExcelSheetResponseDTO() {
        this.excelElements = new ArrayList<>();
    }

    public ExcelSheetResponseDTO(Long sheetId, String excellSheetName, List<ExcelElementDTO> excelElements) {
        this.sheetId = sheetId;
        this.excellSheetName = excellSheetName;
        this.excelElements = excelElements != null ? excelElements : new ArrayList<>();
    }

    public ExcelSheetResponseDTO(Long sheetId, String originalSheetName, String excellSheetName, List<ExcelElementDTO> excelElements) {
        this.sheetId = sheetId;
        this.originalSheetName = originalSheetName;
        this.excellSheetName = excellSheetName;
        this.excelElements = excelElements != null ? excelElements : new ArrayList<>();
    }

    // Getters and Setters
    public Long getSheetId() {
        return sheetId;
    }

    public String getOriginalSheetName() {
        return originalSheetName;
    }

    public String getExcellSheetName() {
        return excellSheetName;
    }

    public List<ExcelElementDTO> getExcelElements() {
        return excelElements;
    }

    public void setExcelElements(List<ExcelElementDTO> excelElements) {
        this.excelElements = excelElements != null ? excelElements : new ArrayList<>();
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExcelSheetResponseDTO that = (ExcelSheetResponseDTO) o;
        return Objects.equals(sheetId, that.sheetId) &&
                Objects.equals(originalSheetName, that.originalSheetName) &&
                Objects.equals(excellSheetName, that.excellSheetName) &&
                Objects.equals(excelElements, that.excelElements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sheetId, originalSheetName, excellSheetName, excelElements);
    }

    // toString
    @Override
    public String toString() {
        return "ExcelSheetResponseDTO{" +
                "sheetId=" + sheetId +
                ", originalSheetName='" + originalSheetName + '\'' +
                ", excellSheetName='" + excellSheetName + '\'' +
                ", excelElements=" + excelElements +
                '}';
    }
}
