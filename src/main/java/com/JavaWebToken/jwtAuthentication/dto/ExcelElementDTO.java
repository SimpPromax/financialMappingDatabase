package com.JavaWebToken.jwtAuthentication.dto;

import java.util.Objects;

public class ExcelElementDTO {
    private Long elementId; // Changed from 'id' to 'elementId' to match entity
    private String excelElement;
    private String exelCellValue;
    private String cellReference;  // ✅ Added cellReference


    // Constructors
    public ExcelElementDTO() {}

    public ExcelElementDTO(Long elementId, String excelElement, String exelCellValue) {
        this.elementId = elementId;
        this.excelElement = excelElement;
        this.exelCellValue = exelCellValue;
    }

    // Getters and Setters
    public Long getElementId() {
        return elementId;
    }

    public void setElementId(Long elementId) {
        this.elementId = elementId;
    }

    public String getExcelElement() {
        return excelElement;
    }

    public void setExcelElement(String excelElement) {
        this.excelElement = excelElement;
    }

    public String getExelCellValue() {
        return exelCellValue;
    }

    public void setExelCellValue(String exelCellValue) {
        this.exelCellValue = exelCellValue;
    }
    public String getCellReference() {
        return cellReference;
    }
    public void setCellReference(String cellReference) {
        this.cellReference = cellReference;
    }

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExcelElementDTO that = (ExcelElementDTO) o;
        return Objects.equals(elementId, that.elementId) &&
                Objects.equals(excelElement, that.excelElement) &&
                Objects.equals(exelCellValue, that.exelCellValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, excelElement, exelCellValue);
    }

    @Override
    public String toString() {
        return "ExcelElementDTO{" +
                "elementId=" + elementId +
                ", excelElement='" + excelElement + '\'' +
                ", exelCellValue='" + exelCellValue + '\'' +
                '}';
    }
}