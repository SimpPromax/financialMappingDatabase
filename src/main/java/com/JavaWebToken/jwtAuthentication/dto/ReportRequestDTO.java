package com.JavaWebToken.jwtAuthentication.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReportRequestDTO {
    private String sheetName;
    private LocalDate startDate;
    private LocalDate endDate;
}