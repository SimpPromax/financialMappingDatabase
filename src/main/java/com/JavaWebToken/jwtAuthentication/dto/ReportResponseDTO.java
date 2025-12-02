package com.JavaWebToken.jwtAuthentication.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ReportResponseDTO {
    private String sheetName;
    private Map<String, Object> cellValues;
    private int totalCellsUpdated;
    private String generatedFileName;
}