package com.JavaWebToken.jwtAuthentication.dto;


import java.util.Objects;

public class ExcelElementDTO {
    private String excelElement;
    private String exelCellValue;

    // Constructors
    public ExcelElementDTO() {}

    public ExcelElementDTO(String excelElement, String exelCellValue) {
        this.excelElement = excelElement;
        this.exelCellValue = exelCellValue;
    }

    // Getters and Setters
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

    // Equals and HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExcelElementDTO that = (ExcelElementDTO) o;
        return Objects.equals(excelElement, that.excelElement) &&
                Objects.equals(exelCellValue, that.exelCellValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(excelElement, exelCellValue);
    }

    // toString
    @Override
    public String toString() {
        return "ExcelElementDTO{" +
                "excelElement='" + excelElement + '\'' +
                ", exelCellValue='" + exelCellValue + '\'' +
                '}';
    }
}