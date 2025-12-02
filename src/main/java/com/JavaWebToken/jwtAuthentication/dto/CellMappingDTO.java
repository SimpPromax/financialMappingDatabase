package com.JavaWebToken.jwtAuthentication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CellMappingDTO {
    private String targetCell;      // exel_cell_value: "B10", "C15", etc.
    private String elementName;     // excel_element: "Total Sales"
    private String coaName;         // coa_name: "Sales Revenue"
    private String coaCode;         // coa_code: "4001"
    private String sqlScript;       // sql_script: "SELECT SUM(...)"
    private Long mappingId;         // mapping_id
    private String sheetName;       // excell_sheet_name: Added this field
}