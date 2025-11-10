package com.JavaWebToken.jwtAuthentication.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExcelSheetResponseDTO {
    private String originalSheetName;
    private String excellSheetName;
    private List<ExcelElementDTO> excelElements;

    // Constructors
    public ExcelSheetResponseDTO() {
        this.excelElements = new ArrayList<>();
    }

    public ExcelSheetResponseDTO(String excellSheetName, List<ExcelElementDTO> excelElements) {
        this.excellSheetName = excellSheetName;
        this.excelElements = excelElements != null ? excelElements : new ArrayList<>();
    }

    public ExcelSheetResponseDTO(String originalSheetName, String excellSheetName, List<ExcelElementDTO> excelElements) {
        this.originalSheetName = originalSheetName;
        this.excellSheetName = excellSheetName;
        this.excelElements = excelElements != null ? excelElements : new ArrayList<>();
    }

    // Getters and Setters
    public String getOriginalSheetName() {
        return originalSheetName;
    }

    public void setOriginalSheetName(String originalSheetName) {
        this.originalSheetName = originalSheetName;
    }

    public String getExcellSheetName() {
        return excellSheetName;
    }

    public void setExcellSheetName(String excellSheetName) {
        this.excellSheetName = excellSheetName;
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
        return Objects.equals(originalSheetName, that.originalSheetName) &&
                Objects.equals(excellSheetName, that.excellSheetName) &&
                Objects.equals(excelElements, that.excelElements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalSheetName, excellSheetName, excelElements);
    }

    // toString
    @Override
    public String toString() {
        return "ExcelSheetResponseDTO{" +
                "originalSheetName='" + originalSheetName + '\'' +
                ", excellSheetName='" + excellSheetName + '\'' +
                ", excelElements=" + excelElements +
                '}';
    }
}